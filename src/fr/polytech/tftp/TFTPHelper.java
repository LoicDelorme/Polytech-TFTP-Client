package fr.polytech.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a TFTP helper.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class TFTPHelper
{
	/**
	 * The timeout value.
	 */
	private final static int TIMEOUT = 30000; // 30 secondes

	/**
	 * The default packet size.
	 */
	private final static int PACKET_SIZE = 516;

	/**
	 * The default data size.
	 */
	private final static int DATA_SIZE = 512;

	/**
	 * The open file error.
	 */
	public static final int OPEN_FILE_ERROR = -1;

	/**
	 * The socket error.
	 */
	public static final int SOCKET_ERROR = -2;

	/**
	 * The network error.
	 */
	public static final int NETWORK_ERROR = -3;

	/**
	 * Send a file.
	 * 
	 * @param localFileName
	 *            The local file name.
	 * @param distantFileName
	 *            The distant file name.
	 * @param serverAddress
	 *            The server address.
	 * @param serverPort
	 *            The server port.
	 * @return CRRV value.
	 */
	public static int sendFile(String localFileName, String distantFileName, InetAddress serverAddress, int serverPort)
	{
		final File localFile = new File(localFileName);
		if (!localFile.exists())
		{
			return OPEN_FILE_ERROR;
		}

		try (final DatagramSocket socket = new DatagramSocket())
		{
			socket.setSoTimeout(TIMEOUT);

			byte[] requestData = TFTPRequestHelper.createConnectionRequest(TFTPRequestHelper.CODE_OPERATION_WRQ, distantFileName, "octet");
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
			socket.send(requestPacket);

			byte[] answerData = new byte[PACKET_SIZE];
			DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);
			socket.receive(answerPacket);

			int answerCodeOperation = answerPacket.getData()[0] * 256 + answerPacket.getData()[1];
			if (answerCodeOperation != TFTPRequestHelper.CODE_OPERATION_ACK)
			{
				if (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_ERROR)
				{

					return answerPacket.getData()[2] * 256 + answerPacket.getData()[3];
				}

				throw new RuntimeException("Unexpected OP code.");
			}

			final FileInputStream fileInputStream = new FileInputStream(localFile);
			byte[] data = new byte[DATA_SIZE];
			int fileOffset = 0;
			int currentBlock = 1;
			int nbDataRead = 0;
			while ((fileOffset < localFile.length()) && (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_ACK))
			{
				nbDataRead = fileInputStream.read(data, 0, DATA_SIZE);
				requestData = TFTPRequestHelper.createDataRequest(currentBlock, data, nbDataRead);
				requestPacket = new DatagramPacket(requestData, requestData.length, answerPacket.getAddress(), answerPacket.getPort());
				socket.send(requestPacket);

				try
				{
					answerPacket = new DatagramPacket(answerData, answerData.length);
					socket.receive(answerPacket);
				}
				catch (SocketTimeoutException e)
				{
					socket.send(requestPacket);

					try
					{
						socket.receive(answerPacket);
					}
					catch (SocketTimeoutException ex)
					{
						fileInputStream.close();
						socket.close();
						return NETWORK_ERROR;
					}
				}

				fileOffset += nbDataRead;
				currentBlock += 1;
				answerCodeOperation = answerPacket.getData()[0] * 256 + answerPacket.getData()[1];
			}

			fileInputStream.close();
			socket.close();

			return (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_ACK ? 0 : answerPacket.getData()[2] * 256 + answerPacket.getData()[3]);
		}
		catch (SocketException e)
		{
			return SOCKET_ERROR;
		}
		catch (IOException e)
		{
			return NETWORK_ERROR;
		}
	}

	/**
	 * Receive a file.
	 * 
	 * @param localFileName
	 *            The local file name.
	 * @param distantFileName
	 *            The distant file name.
	 * @param serverAddress
	 *            The server address.
	 * @param serverPort
	 *            The server port.
	 * @return CRRV value.
	 */
	public static int receiveFile(String localFileName, String distantFileName, InetAddress serverAddress, int serverPort)
	{
		try (final DatagramSocket socket = new DatagramSocket())
		{
			byte[] requestData = TFTPRequestHelper.createConnectionRequest(TFTPRequestHelper.CODE_OPERATION_RRQ, distantFileName, "octet");
			DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
			socket.send(requestPacket);

			byte[] answerData = new byte[PACKET_SIZE];
			DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);
			socket.receive(answerPacket);

			int answerCodeOperation = answerPacket.getData()[0] * 256 + answerPacket.getData()[1];
			if (answerCodeOperation != TFTPRequestHelper.CODE_OPERATION_DATA)
			{
				if (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_ERROR)
				{

					return answerPacket.getData()[2] * 256 + answerPacket.getData()[3];
				}

				throw new RuntimeException("Unexpected OP code.");
			}

			final FileOutputStream fileOutputStream = new FileOutputStream(localFileName);
			final List<Integer> readBlocks = new ArrayList<Integer>();
			while (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_DATA)
			{
				final int blockNumber = (answerPacket.getData()[2] & 0x000000FF) * 256 + (answerPacket.getData()[3] & 0x000000FF);
				if (!readBlocks.contains(blockNumber))
				{
					readBlocks.add(blockNumber);
					fileOutputStream.write(answerPacket.getData(), 4, answerPacket.getLength() - 4);
				}

				requestData = TFTPRequestHelper.createAckRequest(blockNumber);
				requestPacket = new DatagramPacket(requestData, requestData.length, answerPacket.getAddress(), answerPacket.getPort());
				socket.send(requestPacket);

				if (answerPacket.getLength() < PACKET_SIZE)
				{
					break;
				}

				answerPacket = new DatagramPacket(answerData, answerData.length);
				socket.receive(answerPacket);

				answerCodeOperation = answerPacket.getData()[0] * 256 + answerPacket.getData()[1];
			}

			fileOutputStream.close();
			socket.close();

			return (answerCodeOperation == TFTPRequestHelper.CODE_OPERATION_DATA ? 0 : answerPacket.getData()[2] * 256 + answerPacket.getData()[3]);
		}
		catch (SocketException e)
		{
			return SOCKET_ERROR;
		}
		catch (IOException e)
		{
			return NETWORK_ERROR;
		}
	}
}