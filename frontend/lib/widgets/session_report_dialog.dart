import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../models/session/session_model.dart';
import '../providers/service_providers.dart';
import '../utils/report_reason_labels.dart';

enum SessionReportResult { submitted, alreadyReported }

class SessionReportDialog extends ConsumerStatefulWidget {
  final SessionModel session;
  final String reportedUserId;
  final String reportedUserName;

  const SessionReportDialog({
    super.key,
    required this.session,
    required this.reportedUserId,
    required this.reportedUserName,
  });

  @override
  ConsumerState<SessionReportDialog> createState() =>
      _SessionReportDialogState();
}

class _SessionReportDialogState extends ConsumerState<SessionReportDialog> {
  static const List<_ReportReasonOption> _reasonOptions = [
    _ReportReasonOption('NO_SHOW'),
    _ReportReasonOption('NO_RESPONSE'),
    _ReportReasonOption('INAPPROPRIATE_BEHAVIOUR'),
    _ReportReasonOption('OTHER'),
  ];

  final TextEditingController _descriptionController = TextEditingController();
  String? _selectedReason;
  bool _isSubmitting = false;

  @override
  void dispose() {
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final reason = _selectedReason;
    if (reason == null || _isSubmitting) return;

    setState(() => _isSubmitting = true);
    try {
      await ref
          .read(sessionServiceProvider)
          .createPrivateReport(
            sessionId: widget.session.id,
            reportedUserId: widget.reportedUserId,
            reasonCode: reason,
            description: _descriptionController.text,
          );
      if (!mounted) return;
      Navigator.pop(context, SessionReportResult.submitted);
    } catch (e) {
      if (!mounted) return;
      final message = e.toString().replaceAll('Exception: ', '');
      if (message.toLowerCase().contains('already reported')) {
        Navigator.pop(context, SessionReportResult.alreadyReported);
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Report could not be sent: $message'),
          backgroundColor: Colors.redAccent,
        ),
      );
      setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      title: Text(
        'Report ${widget.reportedUserName}',
        textAlign: TextAlign.center,
        style: TextStyle(
          color: isDark ? Colors.white : Colors.black87,
          fontWeight: FontWeight.w900,
        ),
      ),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Select a reason',
              style: TextStyle(
                color: isDark ? Colors.white70 : Colors.black54,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(
              initialValue: _selectedReason,
              isExpanded: true,
              decoration: InputDecoration(
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(
                    color: AppColors.primaryBlue,
                    width: 1.4,
                  ),
                ),
              ),
              items: _reasonOptions.map((option) {
                return DropdownMenuItem<String>(
                  value: option.value,
                  child: Text(option.label),
                );
              }).toList(),
              onChanged: _isSubmitting
                  ? null
                  : (value) {
                      setState(() => _selectedReason = value);
                    },
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _descriptionController,
              enabled: !_isSubmitting,
              maxLines: 4,
              maxLength: 2000,
              decoration: InputDecoration(
                hintText: 'Add details (optional)',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: const BorderSide(
                    color: AppColors.primaryBlue,
                    width: 1.4,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
      actions: [
        Row(
          children: [
            Expanded(
              child: OutlinedButton(
                onPressed: _isSubmitting ? null : () => Navigator.pop(context),
                style: OutlinedButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                child: const Text('Cancel'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: ElevatedButton.icon(
                icon: _isSubmitting
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Icon(
                        Icons.flag_outlined,
                        color: Colors.white,
                        size: 18,
                      ),
                label: const Text(
                  'Send',
                  style: TextStyle(color: Colors.white),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryBlue,
                  disabledBackgroundColor: AppColors.primaryBlue.withValues(
                    alpha: 0.45,
                  ),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                onPressed: _selectedReason == null || _isSubmitting
                    ? null
                    : _submit,
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class _ReportReasonOption {
  final String value;

  const _ReportReasonOption(this.value);

  String get label => ReportReasonLabels.label(value);
}
