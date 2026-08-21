package com.fbi.criminal_justice_system.utils;

public final class RequestGuard {

	private long lastIssued;

	public long next() {
		return ++lastIssued;
	}

	public boolean isCurrent(long ticket) {
		return ticket == lastIssued;
	}
}
