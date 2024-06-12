package hongze.myCEX;

public record ApiErrorResponse(ApiError error, String data, String message) {

}
