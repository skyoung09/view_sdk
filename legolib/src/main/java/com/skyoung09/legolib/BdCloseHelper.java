package com.skyoung09.legolib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 安全关闭类 <br>
 * 内部有try-cathc捕获异常并使用BdLog.e打印错误日志。
 * 内部方法都为static方法，可直接使用，如：BdCloseHelper.close(is);
 * 
 */
public class BdCloseHelper {

	/**
	 * 关闭给定的输入流. <BR>
	 * 
	 * @param inStream
	 */
	public static void close(InputStream inStream) {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 关闭给定的流.
	 * 
	 * @param stream
	 */
	public static void close(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (Throwable e) {
			}
		}
	}

	/**
	 * 关闭给定的输出流. <BR>
	 * 
	 * @param outStream
	 */
	public static void close(OutputStream outStream) {
		if (outStream != null) {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 关闭给定的输出流. <BR>
	 * 
	 * @param writer
	 */
	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 关闭给定的Socket.
	 * 
	 * @param socket
	 *            给定的Socket
	 */
	public static void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 关闭给定的Reader类型输入流
	 * 
	 * @param reader
	 */
	public static void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 关闭给定的连接
	 * 
	 * @param conn
	 */
	public static void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 关闭给定的PreparedStaement 实例
	 * 
	 * @param ps
	 */
	public static void close(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 关闭给定的ResultSet实例。
	 * 
	 * @param rs
	 */
	public static void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 关闭给定的Statement实例
	 * 
	 * @param st
	 */
	public static void close(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * 关闭给定的游标
	 * 
	 * @param c
	 */
	public static void close(Cursor c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 关闭给定的SQLiteDataBase数据库
	 * 
	 * @param db
	 */
	public static void close(SQLiteDatabase db) {
		if (db != null) {
			try {
				db.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 关闭给定的HttpUrlConnection连接
	 * 
	 * @param mConn
	 */
	public static void close(HttpURLConnection mConn) {
		if (mConn != null) {
			try {
				mConn.disconnect();
			} catch (Exception e) {
			}
		}
	}

}
