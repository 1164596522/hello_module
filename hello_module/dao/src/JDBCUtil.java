import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/*
 * ���ݲ㣺���ӷ��ʲ������ݿ����
 */
public class JDBCUtil {
	
	//����
	public static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	//�����ַ���
	public static String url = "jdbc:sqlserver://localhost:1433;DatabaseName=BookDB";
	//�û���
	public static String name = "sa";
	//����
	public static String pwd = "";

	//��������
	static{
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//��ȡ���Ӷ���ķ���
	public static Connection getConn() {
		try {
			return DriverManager.getConnection(url, name, pwd);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//�ͷ���Դ�ķ���
	public static void close(Connection conn,Statement stmt,ResultSet rs) {
		try {
			if(rs!=null) rs.close();
			if(stmt!=null) stmt.close();
			if(conn!=null) conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//ִ����ɾ�ĵķ���
	public static int execUpdate(String sql){
		//��ȡ���Ӷ���
		Connection conn = getConn();
		//�������ִ�ж���
		Statement stmt = null;
		
		try {
			//��ȡ���ִ�ж���
			stmt = conn.createStatement();
			//���ͽ��յ�����䵽���ݿⲢִ��
			return stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			//�ͷ���Դ
			close(conn,stmt,null);
		}
		
		return 0;
	}
	
	//ִ����ɾ�ĵķ���(Ԥ����)
	public static int preparedUpdate(String sql,Object...args){
		//��ȡ���Ӷ���
		Connection conn = getConn();
		//�������ִ�ж���(Ԥ����)
		PreparedStatement stmt = null;
		
		try {
			//��ȡ���ִ�ж���(Ԥ����)
			stmt = conn.prepareStatement(sql);
			
			//���ò���
			for (int i = 0; i < args.length; i++) {
				stmt.setObject(i+1, args[i]);
			}
			
			//���ͽ��յ�����䵽���ݿⲢִ��
			return stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			//�ͷ���Դ
			close(conn,stmt,null);
		}
		
		return 0;
	}
	
	//ִ�в�ѯ�ķ���(Ԥ����)
	public static <T> List<T> preparedQuery(Class<T> classObj,String sql,Object...args){
				
		//��ȡ���Ӷ���
		Connection conn = getConn();
		//�������ִ�ж���(Ԥ����)
		PreparedStatement stmt = null;
		//������������
		ResultSet rs = null;
		
		try {
			//��ȡ���ִ�ж���(Ԥ����)
			stmt = conn.prepareStatement(sql);
			
			//���ò���
			for (int i = 0; i < args.length; i++) {
				stmt.setObject(i+1, args[i]);
			}
			
			//���ͽ��յ�����䵽���ݿⲢִ��
			rs = stmt.executeQuery();
			
			//��ȡ���������������Ϣ�Ķ���
			ResultSetMetaData rsmd = rs.getMetaData();
			
			List<T> list = new ArrayList<T>();
			
			while(rs.next()){
				
				//ͨ�����䶯̬��ȡ����Ϣ����������
				T obj = (T)classObj.getConstructor().newInstance();
				
				//�������������������н���ѭ��
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					//��ȡ��i�е�����
					String fieldName = rsmd.getColumnName(i);
					String typeName = rsmd.getColumnTypeName(i);
					//ͨ�������ȡ�����ĳ������
					Field field = classObj.getDeclaredField(fieldName);
					field.setAccessible(true);
					//��ĳ�������ĳ�����Ը�ֵ
					if("date".equals(typeName)){
						field.set(obj, rs.getString(i));
					}else if("decimal".equals(typeName)){
						field.set(obj, rs.getDouble(i));
					}else if("money".equals(typeName)){
						field.set(obj, rs.getDouble(i));
					}else{
						field.set(obj, rs.getObject(i));
					}
				}
				
				list.add(obj);
			}
			
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			//�ͷ���Դ
			close(conn,stmt,rs);
		}
		
		return null;
	}
	
	/*
	//ִ�в�ѯ�ķ���(Ԥ�����ע��)
	public static <T> List<T> preparedQuery(Class<T> classObj,String sql,Object...args){
		//��ȡ���Ӷ���
		Connection conn = getConn();
		//�������ִ�ж���(Ԥ����)
		PreparedStatement stmt = null;
		//������������
		ResultSet rs = null;
		
		try {
			//��ȡ���ִ�ж���(Ԥ����)
			stmt = conn.prepareStatement(sql);
			
			//���ò���
			for (int i = 0; i < args.length; i++) {
				stmt.setObject(i+1, args[i]);
			}
			
			//���ͽ��յ�����䵽���ݿⲢִ��
			rs = stmt.executeQuery();
			
			//��ȡ���������������Ϣ�Ķ���
			ResultSetMetaData rsmd = rs.getMetaData();
			
			List<T> list = new ArrayList<T>();
			
			while(rs.next()){
				//ͨ�����䶯̬��ȡ����Ϣ����������
				T obj = classObj.getConstructor().newInstance();
				//ѭ��һ��ȡ��ǰ�е�һ������
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					//��ȡ��i�е�����
					String dbFieldName = rsmd.getColumnName(i);
					//ͨ�������ȡ�������������
					Field[] fields = classObj.getDeclaredFields();
					
					for(Field field : fields){
						//�жϵ�ǰ�������Ƿ���FieldName���͵�ע��
						if(field.isAnnotationPresent(FieldName.class)){
							//��ȡע����Ϣ
							String fieldName = field.getAnnotation(FieldName.class).value();
							//�ж�ע���ֵ�Ƿ������ݿ����ֶ���һ��
							if(dbFieldName.equals(fieldName)){
								field.setAccessible(true);
								field.set(obj, rs.getObject(i));
								break;
							}
						}
					}
				}
				
				list.add(obj);
			}
			
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			//�ͷ���Դ
			close(conn,stmt,rs);
		}
		
		return null;
	}
	*/
}
