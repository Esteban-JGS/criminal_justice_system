package com.fbi.cjs.ws.mocks;

import com.fbi.cjs.ws.repository.AgentRepository;
import com.fbi.cjs.ws.repository.AgentRepositoryContractTest;

class MockAgentRepositoryTest extends AgentRepositoryContractTest {

	@Override
	protected AgentRepository newRepository() {
		MockAgentRepository repository = new MockAgentRepository();
		repository.seed(); // fuera de CDI nadie invoca @PostConstruct
		return repository;
	}
}
