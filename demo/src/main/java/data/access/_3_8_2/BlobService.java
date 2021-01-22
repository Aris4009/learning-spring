package data.access._3_8_2;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Service;

@Service
public class BlobService {

	private final JdbcTemplate jdbcTemplate;

	private final LobHandler lobHandler = new DefaultLobHandler();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public BlobService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insertBlob() {
		final InputStream inputStream;
		try {
			ClassPathResource classPathResource = new ClassPathResource("complexObject.xml");
			File file = classPathResource.getFile();
			inputStream = classPathResource.getInputStream();

			String sql = "insert into test_blob_clob(b_lob) values(?)";
			this.jdbcTemplate.execute(sql, new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
				@Override
				protected void setValues(PreparedStatement ps, LobCreator lobCreator)
						throws SQLException, DataAccessException {
//					lobCreator.setBlobAsBinaryStream(ps, 1, inputStream, (int) file.length());
					lobCreator.setBlobAsBinaryStream(ps, 1, inputStream, -1);
				}
			});
			inputStream.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public List<Map<String, Object>> queryList() {
		String sql = "select * from test_blob_clob";
		return jdbcTemplate.query(sql, new RowMapper<Map<String, Object>>() {
			@Override
			public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String, Object> results = new HashMap<String, Object>();
				byte[] blobBytes = lobHandler.getBlobAsBytes(rs, "b_lob");
				results.put("id", rs.getInt(1));
				results.put("b_lob", blobBytes);
				results.put("b_lob_str", new String(blobBytes));
				return results;
			}
		});
	}
}
