package model;

import java.io.File;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FileInfo {

	private String jobId;
	private File file;
	private String fileName;
	private Date modifiedDate;
	private String fileTransferStatus;
	private Date processingStartTimestamp;
	private Date processingEndTimestamp;
	private String sourceFileArchivalStatus;
	private String sourceFileDeletionStatus;

}
