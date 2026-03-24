import java.sql.*;

public class TempDbCheck {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require&ApplicationName=nativeflow-db-check";
        try (Connection connection = DriverManager.getConnection(url, "postgres.fmqywfgjrktrfebgmfhg", "bnjrho12p!A")) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select table_name from information_schema.tables where table_schema = 'public' and table_name in (?, ?, ?) order by table_name")) {
                statement.setString(1, "subscription_plans");
                statement.setString(2, "user_subscriptions");
                statement.setString(3, "billing_transactions");
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1));
                    }
                }
            }
        }
    }
}
