package entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

import lombok.Data;

@Entity
@Table(name = "public.fittle_file_audit_history")
@Data
public class FittleFileAuditHistoryEntity {
	
	@Id
	@Column(name = "file_job_id")
	public String fileJobId;
	
	@Column(name = "file_name")
	public String fileName;
	
	@Column(name = "file_transfer_status")
	public String fileTransferStatus;
	
	@Column(name = "processing_start_ts")
	public Date processingStartTimestamp;
	
	@Column(name = "processing_end_ts")
	public Date processingEndTimestamp;
	
	@Column(name = "src_file_arc_status")
	public String sourceFileArchivalStatus;
	
	@Column(name = "src_file_del_status")
	public String sourceFileDeletionStatus;

}
