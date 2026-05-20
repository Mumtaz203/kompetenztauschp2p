package de.thws.kompetenz.user.adapter.in.rest.dto.user;

import java.util.List;

public class GetAllUsersResponse {

	private List<GetUserResponse> users;

	public GetAllUsersResponse() {
	}

	public GetAllUsersResponse(List<GetUserResponse> users) {
		this.users = users;
	}

	public List<GetUserResponse> getUsers() {
		return users != null ? users : List.of();
	}

	public void setUsers(List<GetUserResponse> users) {
		this.users = users;
	}
}
