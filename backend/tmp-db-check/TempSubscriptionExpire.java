import java.sql.*;

public class TempSubscriptionExpire {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&ApplicationName=nativeflow-subscription-expire";
        try (Connection connection = DriverManager.getConnection(url, "postgres.fmqywfgjrktrfebgmfhg", "bnjrho12p!A");
             PreparedStatement statement = connection.prepareStatement(
                     "update user_subscriptions set status = 'expired', current_period_end = now() - interval '1 day', ended_at = now() where user_id = cast(? as uuid)")) {
            statement.setString(1, args[0]);
            statement.executeUpdate();
        }
    }
}
