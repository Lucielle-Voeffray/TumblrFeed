package services;

public class Error {

    public static void report(LogType type, String fileName, String methodName, int tryIndex, Exception e) {
        System.out.printf("%s [ %s ] FAILURE %s method: %s Try: %d | Error Message: %s%n", java.time.LocalDateTime.now(), type, fileName, methodName, tryIndex, e);
    }

    public static void report(LogType type, String fileName, String methodName, int tryIndex, String ErrorMessage) {
        System.out.printf("%s [ %s ] FAILURE %s method: %s Try: %d | Error Message: %s%n", java.time.LocalDateTime.now(), type, fileName, methodName, tryIndex, ErrorMessage);
    }

}
