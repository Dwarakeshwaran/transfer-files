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
	public String fileJobId;

	@Column(name = "src_server_protocol")
	public String sourceServerProtocol;

	@Column(name = "src_server_host_name")
	public String sourceServerHostName;

	@Column(name = "src_file_path")
	public String sourceFilePath;

	@Column(name = "trg_server_protocol")
	public String targetServerProtocol;

	@Column(name = "trg_server_host_name")
	public String targetServerHostName;

	@Column(name = "trg_file_path")
	public String targetFilePath;

	@Column(name = "src_archival_path")
	public String sourceArchivalPath;

	@Column(name = "delete_after_suc")
	public String deleteAfterSuccess;

	@Column(name = "file_extension")
	public String fileExtension;

}
