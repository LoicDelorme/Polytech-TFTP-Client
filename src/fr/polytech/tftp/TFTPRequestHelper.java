package fr.polytech.tftp;

import java.nio.ByteBuffer;

/**
 * This class represents a TFTP request helper.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class TFTPRequestHelper
{
	/**
	 * The code operation RRQ.
	 */
	public static final byte CODE_OPERATION_RRQ = 1;

	/**
	 * The code operation WRQ.
	 */
	public static final byte CODE_OPERATION_WRQ = 2;

	/**
	 * The code operation DATA.
	 */
	public static final byte CODE_OPERATION_DATA = 3;

	/**
	 * The code operation ACK.
	 */
	public static final byte CODE_OPERATION_ACK = 4;

	/**
	 * The code operation ERROR.
	 */
	public static final byte CODE_OPERATION_ERROR = 5;

	/**
	 * Create a connection request.
	 * 
	 * @param codeOperation
	 *            The code operation (WRQ or RRQ).
	 * @param localFileName
	 *            The local file name.
	 * @param mode
	 *            The mode.
	 * @return The built connection request.
	 */
	public static byte[] createConnectionRequest(int codeOperation, String localFileName, String mode)
	{
		final StringBuilder connectionRequestBuilder = new StringBuilder();
		// CODE OPERATION
		connectionRequestBuilder.append("\0");
		switch (codeOperation)
		{
			case CODE_OPERATION_RRQ:
				connectionRequestBuilder.append("\1");
				break;
			case CODE_OPERATION_WRQ:
				connectionRequestBuilder.append("\2");
				break;
			default:
				break;
		}
		// FILE NAME
		connectionRequestBuilder.append(localFileName);
		// SEPARATOR
		connectionRequestBuilder.append("\0");
		// MODE
		connectionRequestBuilder.append(mode);
		// SEPARATOR
		connectionRequestBuilder.append("\0");

		return connectionRequestBuilder.toString().getBytes();
	}

	/**
	 * Create a data request.
	 * 
	 * @param blockNumber
	 *            The block number.
	 * @param data
	 *            The data.
	 * @param dataLength
	 *            The data length.
	 * @return The built data request.
	 */
	public static byte[] createDataRequest(int blockNumber, byte[] data, int dataLength)
	{
		final byte[] blockNumberRepresentation = ByteBuffer.allocate(4).putInt(blockNumber).array();

		final byte[] dataRequest = new byte[4 + dataLength];
		// CODE OPERATION
		dataRequest[0] = 0;
		dataRequest[1] = CODE_OPERATION_DATA;
		// BLOCK NUMBER
		dataRequest[2] = blockNumberRepresentation[2];
		dataRequest[3] = blockNumberRepresentation[3];
		// DATA
		for (int offset = 0; offset < dataLength; offset++)
		{
			dataRequest[4 + offset] = data[offset];
		}

		return dataRequest;
	}

	/**
	 * Create an ACK request.
	 * 
	 * @param blockNumber
	 *            The block number.
	 * @return The built ACK request.
	 */
	public static byte[] createAckRequest(int blockNumber)
	{
		final byte[] blockNumberRepresentation = ByteBuffer.allocate(4).putInt(blockNumber).array();

		final byte[] ackRequest = new byte[4];
		// CODE OPERATION
		ackRequest[0] = 0;
		ackRequest[1] = CODE_OPERATION_ACK;
		// BLOCK NUMBER
		ackRequest[2] = blockNumberRepresentation[2];
		ackRequest[3] = blockNumberRepresentation[3];

		return ackRequest;
	}
}