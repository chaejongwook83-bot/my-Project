


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask; 

public class DB_CALL
{ 
	
	public static void main(String[] args) {
			DB_CALL dc = new DB_CALL();
			dc.checkDB();
		
	}
	
	public void checkDB() { 
		
//		1. 프로젝트에 ojdbc7.jar 자료파일 라이브러리를 포함시킨다.
		String driver = "oracle.jdbc.driver.OracleDriver";
		Connection con = null;
		String url = "jdbc:oracle:thin:@10.134.254.185:1522:DSPCMDBS";
		String sql;
		Statement stmt = null;
//		2. OracleDriver 클래스를 JVM에 로드 시킨다.
		try {
			Class.forName(driver);//이 드라이버로 접속한다.
			System.out.println("JDBC 드라이버 로드 성공 꺅~*^3^*!");
//		3. 로드 후, java.sql.DriverManager.getConnection()로 Connection con = DriverManager.getConnection();
			con = DriverManager.getConnection(url, "DEVPCM", "Pcm_0209!"); 
			System.out.println("데이버 베이스 연결 성공!! 잘했엉~!!>,<");
//		4. 커낵션 객체에서 Statement 객체를 얻는다.
			stmt = con.createStatement();
			sql = "SELECT 1 AS FFFF FROM DUAL";
			//sql +="(2,'주종선','js@naver.com','010-1234-1534')";
//		5. Statement 객체에 있는 ExecuteQuery문자에 SQL문장을 넣는다. 
			ResultSet res = stmt.executeQuery(sql);
			if(res != null) {
				System.out.println("조회~~^0^~~"+res.getRow());
			}else {
				System.out.println("실패당 ㅠㅠ");
			}
			
			 LocalTime now = LocalTime.now();         // 현재시간 출력        
			 // 포맷 정의하기        
			 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시 mm분 ss초");         
			 // 포맷 적용하기        
			 String formatedNow = now.format(formatter);         // 포맷 적용된 현재 시간 출력        System.out.println(formatedNow);  // 06시 20분 57초
			 
			
			while(res.next()) { //rs.next()를 통해 다음행을 내려갈 수 있으면 true를 반환하고, 커서를 한칸 내린다. 다음행이 없으면 false를 반환한다.
				//System.out.println(res.getInt(0) + "\t" + res.getString(0)); //getInt(1)은 컬럼의 1번째 값을 Int형으로 가져온다. / getString(2)는 컬럼의 2번째 값을 String형으로 가져온다.
				System.out.println(res.getString("FFFF")+" >>>>>>>>>>>>>> "+formatedNow);
			}
			
			res.close();
		} catch (Exception e) {
			System.out.println("JDBC 드라이버 로드 실패 ㅠㅠㅠㅠ!");
			e.printStackTrace();
		}//예외 발생가능
		finally {
			try {
//		6.  Statement.close()	Connection.close()
				stmt.close();
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
			Timer timer = new Timer();
	           TimerTask task = new TimerTask() {
	               @Override
	               public void run() {
	                   System.out.println("checkDB()");
	                   checkDB();
	               }
	           };
	           timer.schedule(task, 240000); // 240초 후 실행 
			
		}
		
	}
	
}