package entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

import lombok.Data;

@Entity
@Table(name = "fittle_file_audit_history")
@Data
public class FittleFileAuditHistoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "file_job_id")
	private String fileJobId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "file_transfer_status")
	private String fileTransferStatus;

	@Column(name = "processing_start_ts")
	private Date processingStartTimestamp;

	@Column(name = "processing_end_ts")
	private Date processingEndTimestamp;

	@Column(name = "src_file_arc_status")
	private String sourceFileArchivalStatus;

	@Column(name = "src_file_del_status")
	private String sourceFileDeletionStatus;

}
