package com.fbi.cjs.shared.api;

import java.util.List;

public class ApiResponse<T> {

	private int status;
	private ResponseCode code;
	private String message;
	private T data;

	private List<String> errors;

	public ApiResponse() {
	}

	public ApiResponse(ResponseCode code, String message, T data, List<String> errors) {
		this.status = code.getHttpStatus();
		this.code = code;
		this.message = message;
		this.data = data;
		this.errors = errors;
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(ResponseCode.OK, message, data, null);
	}

	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(ResponseCode.CREATED, message, data, null);
	}

	public static <T> ApiResponse<T> error(ResponseCode code, String message) {
		return new ApiResponse<>(code, message, null, null);
	}

	public static <T> ApiResponse<T> error(ResponseCode code, String message, List<String> errors) {
		return new ApiResponse<>(code, message, null, errors);
	}

	public boolean isSuccess() {
		return status >= 200 && status < 300;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ResponseCode getCode() {
		return code;
	}

	public void setCode(ResponseCode code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
