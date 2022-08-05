package model;

import java.io.File;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FileInfo {

	private File file;
	private String fileName;
	private Date modifiedDate;

}
