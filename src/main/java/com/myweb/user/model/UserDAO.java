package com.myweb.user.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.myweb.util.JdbcUtil;


public class UserDAO {
	
	//DAO는 불필요하게 여러개 만들 필요가 없기 때문에 객체가 한개만 생성되도록 
	//singleton 형식으로 설계
	
	//1. 나 자신의 객체를 1개 생성하고, private을 붙임
	private static UserDAO instance = new UserDAO();
	
	//2. 직접 객체를 생성할 수 없도록 생성자에도 private을 붙임
	private UserDAO() {
		
		//커넥션풀에 사용할 객체를 얻어옴
		try {
			InitialContext init = new InitialContext(); //시작설정 객체
			
			ds = (DataSource)init.lookup("java:comp/env/jdbc/oracle");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//3. 객체 생성을 요구할 때 getter메서드를 사용해서 1번의 객체를 반환
	public static UserDAO getInstance() {
		return instance;
	}
	///////////////////////////////////////////////////////////////////
	//커넥션 풀 객체정보
	private DataSource ds;

	//아이디 중복검사
	public int findUser(String id) {
		int result = 0;
		
		String sql = "SELECT * FROM USERS WHERE ID = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {//다음 행이 있다는 것은 유저가 있다는 의미
				result = 1; //유저가 있다는 뜻
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, rs); 
		}
		
		return result;
	}
	//회원가입
	public void insertUser(String id, String pw, String name, String email, String gender) {
		
		String sql = "INSERT INTO USERS(ID, PW, NAME, EMAIL, GENDER) VALUES (?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			pstmt.setString(3, name);
			pstmt.setString(4, email);
			pstmt.setString(5, gender);
			
			pstmt.executeUpdate();//i, u, d구문은 executeUpdate()구문으로 실행함
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
	}

	//로그인
	public UserDTO login(String id, String pw) {
		String sql = "SELECT * FROM USERS WHERE ID = ? AND PW = ?";
		
		UserDTO dto = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				
				String name = rs.getString("name");
				String gender = rs.getString("gender");
				String email = rs.getString("email");
				
				dto = new UserDTO(id, null, name, email, gender, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, rs);
		}
		
		return dto;
	}
	//회원정보조회
	public UserDTO getInfo(String id) {
		UserDTO dto = new UserDTO();
		
		String sql = "SELECT * FROM USERS WHERE ID = ?";
//		UserDTO dto = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			conn = ds.getConnection(); //DB연결하기 위한 객체
			pstmt = conn.prepareStatement(sql); //sql 실행하기 위한 객체
			pstmt.setString(1, id); //? 채우기 위한 설정값
			
			rs = pstmt.executeQuery(); //select 문은 executequery로 실행
			
			if(rs.next()) { //1행만 출력하기에 if 사용 다수면 while//다음이 있으면 다음행으로 접근
				
				//ORM
				String iden = rs.getString("id"); //String형으로 컬럼명을 얻는다.
				String name = rs.getString("name");
				String gender = rs.getString("gender");
				String email = rs.getString("email");
				Timestamp regdate = rs.getTimestamp("regdate");
				
				//setter - dto 안에 값이 저장됨
				dto.setId(iden);
				dto.setName(name);
				dto.setEmail(email);
				dto.setGender(gender);
				dto.setRegdate(regdate);
//				dto = new UserDTO(iden, null, name, email, gender, null);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, rs);
		}
		
		return dto;
	
	}
	
	//회원정보 수정 - 성공시 1 반환, 실패시 0 반환
	public int update(UserDTO dto) {
		int result = 0; //실패하면 0이 됨
		
		String sql = "UPDATE USERS SET PW = ?, NAME = ?, EMAIL = ?, GENDER = ? WHERE ID = ?";
		Connection conn = null;
		PreparedStatement pstmt = null;
		//rs는 필요없음
		
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, dto.getPw());
			pstmt.setString(2, dto.getName());
			pstmt.setString(3, dto.getEmail());
			pstmt.setString(4, dto.getGender());
			pstmt.setString(5, dto.getId());
			
			//업데이트 실패하면 0반환 실패, 업데이트가 되면 1반환 성공
			result = pstmt.executeUpdate(); //insert, update, delete 구문 executeUpdate()로 실행
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
		
		return result;
	}
	//삭제 메서드
	public void delete(String id) {
		String sql = "DELETE FROM USERS WHERE ID = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, id);
			pstmt.executeUpdate(); //반환을 받지 않을거라면 끝
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
	}
}