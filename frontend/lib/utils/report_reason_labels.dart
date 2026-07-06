class ReportReasonLabels {
  static String label(String reasonCode) {
    switch (reasonCode) {
      case 'NO_SHOW':
        return 'Did not show up';
      case 'NO_RESPONSE':
        return 'Stopped responding';
      case 'INAPPROPRIATE_BEHAVIOUR':
        return 'Inappropriate behaviour';
      case 'OTHER':
        return 'Other issue';
      default:
        return reasonCode
            .toLowerCase()
            .split('_')
            .map(
              (part) => part.isEmpty
                  ? part
                  : '${part[0].toUpperCase()}${part.substring(1)}',
            )
            .join(' ');
    }
  }
}
