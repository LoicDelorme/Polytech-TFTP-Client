package fr.polytech.tftp;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * This class represents a TFTP client controller.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class TFTPClientController implements Initializable
{
	/**
	 * The IP address text field.
	 */
	@FXML
	private TextField ipAddress;

	/**
	 * The port text field.
	 */
	@FXML
	private TextField port;

	/**
	 * The select file to send button.
	 */
	@FXML
	private Button selectFileToSendButton;

	/**
	 * The file path text field.
	 */
	@FXML
	private TextField filePathTextField;

	/**
	 * The send file button.
	 */
	@FXML
	private Button sendFileButton;

	/**
	 * The file name to receive.
	 */
	@FXML
	private TextField fileNameToReceive;

	/**
	 * The select directory to receive button.
	 */
	@FXML
	private Button selectDirectoryToReceiveButton;

	/**
	 * The directory name text field.
	 */
	@FXML
	private TextField directoryNameTextField;

	/**
	 * The receive file button.
	 */
	@FXML
	private Button receiveFileButton;

	/**
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.selectFileToSendButton.setOnAction(e ->
		{
			final FileChooser fileChooser = new FileChooser();
			final File selectedFile = fileChooser.showOpenDialog(null);
			if (selectedFile != null)
			{
				this.filePathTextField.setText(selectedFile.getAbsolutePath());
			}
			else
			{
				this.filePathTextField.setText("");
			}
		});

		this.sendFileButton.setOnAction(e ->
		{
			try
			{
				final String localFileName = this.filePathTextField.getText();
				final String distantFileName = localFileName.substring(localFileName.lastIndexOf("\\"), localFileName.length());
				final InetAddress serverAddress = InetAddress.getByName(this.ipAddress.getText());
				final int serverPort = Integer.parseInt(this.port.getText());

				final int sendFile = TFTPHelper.sendFile(localFileName, distantFileName, serverAddress, serverPort);

				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Code de retour");
				alert.setHeaderText(null);
				alert.setContentText("Code de retour : " + sendFile);

				alert.showAndWait();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		});

		this.selectDirectoryToReceiveButton.setOnAction(e ->
		{
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			final File selectedDirectory = directoryChooser.showDialog(null);
			if (selectedDirectory != null)
			{
				this.directoryNameTextField.setText(selectedDirectory.getAbsolutePath());
			}
			else
			{
				this.directoryNameTextField.setText("");
			}

		});

		this.receiveFileButton.setOnAction(e ->
		{
			try
			{
				final String distantFileName = this.fileNameToReceive.getText();
				final String localDirectoryName = this.directoryNameTextField.getText() + File.separator + distantFileName;
				final InetAddress serverAddress = InetAddress.getByName(this.ipAddress.getText());
				final int serverPort = Integer.parseInt(this.port.getText());

				final int receiveFile = TFTPHelper.receiveFile(localDirectoryName, distantFileName, serverAddress, serverPort);

				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Code de retour");
				alert.setHeaderText(null);
				alert.setContentText("Code de retour : " + receiveFile);

				alert.showAndWait();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		});
	}
}