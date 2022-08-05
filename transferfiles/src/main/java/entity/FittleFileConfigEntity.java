package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "fittle_file_configuration")
@Data
@ToString
public class FittleFileConfigEntity {

	@Id
	@Column(name = "file_job_id")
	private String fileJobId;

	@Column(name = "src_server_protocol")
	private String sourceServerProtocol;

	@Column(name = "src_server_credentials")
	private String sourceServerCredentials;

	@Column(name = "src_server_host_name")
	private String sourceServerHostName;

	@Column(name = "src_file_path")
	private String sourceFilePath;

	@Column(name = "trg_server_protocol")
	private String targetServerProtocol;

	@Column(name = "trg_server_credentials")
	private String targetServerCredentials;

	@Column(name = "trg_server_host_name")
	private String targetServerHostName;

	@Column(name = "trg_file_path")
	private String targetFilePath;

	@Column(name = "src_archival_path")
	private String sourceArchivalPath;

	@Column(name = "delete_after_suc")
	private String deleteAfterSuccess;

	@Column(name = "file_extension")
	private String fileExtension;

}
