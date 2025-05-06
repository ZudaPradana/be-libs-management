package zydd.org.libsmanagement.Commons.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse<T>{
    private String status;
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message,T data){
        return new ApiResponse<>("SUCCESS", 200, message ,data);
    }

    public static <T> ApiResponse<T> success(int code, String message,T data){
        return new ApiResponse<>("Success", code, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message,T data){
        return new ApiResponse<>("ERROR", code, message,data);
    }
}
