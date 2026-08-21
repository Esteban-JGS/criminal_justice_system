package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.ws.repository.CriminalRepository;
import com.fbi.cjs.ws.repository.CriminalRepositoryContractTest;

class MockCriminalRepositoryTest extends CriminalRepositoryContractTest {

	@Override
	protected CriminalRepository newRepository() {
		MockCriminalRepository repository = new MockCriminalRepository();
		repository.seed(); // fuera de CDI nadie invoca @PostConstruct
		return repository;
	}
}
