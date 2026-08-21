package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.ws.repository.UserRepository;
import com.fbi.cjs.ws.repository.UserRepositoryContractTest;

class MockUserRepositoryTest extends UserRepositoryContractTest {

	@Override
	protected UserRepository newRepository() {
		MockUserRepository repository = new MockUserRepository();
		repository.seed(); // fuera de CDI nadie invoca @PostConstruct
		return repository;
	}
}
