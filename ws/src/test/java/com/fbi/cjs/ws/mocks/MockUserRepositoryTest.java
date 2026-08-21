package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.ws.repository.UserRepository;
import com.fbi.cjs.ws.repository.UserRepositoryContractTest;

/** Aplica el contrato de repositorio a la implementación en memoria. */
class MockUserRepositoryTest extends UserRepositoryContractTest {

	@Override
	protected UserRepository newRepository() {
		MockUserRepository repository = new MockUserRepository();
		repository.seed(); // fuera de CDI nadie invoca @PostConstruct
		return repository;
	}
}
