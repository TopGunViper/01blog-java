package edu.ouc.chain;

public class UserServiceImpl implements UserService {

	@Override
	public void update(Object obj) {
		System.out.println("update user info.");		
	}

	@Override
	public void delete(Object obj) {
		System.out.println("delete user info.");		
	}
}
