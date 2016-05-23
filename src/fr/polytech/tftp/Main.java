package fr.polytech.tftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class represents
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class Main
{
	/**
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException
	{
		final int sendFile = TFTPHelper.receiveFile("D:/Downloads/TO SEND/delorme.png", "delormel.png", InetAddress.getByName("134.214.119.203"), 69);
		System.out.println(sendFile);
		final int sendFile2 = TFTPHelper.receiveFile("D:/Downloads/TO SEND/poly_ARAR_3A_INFO.pdf", "ARAR_3A_INFO.pdf", InetAddress.getByName("134.214.119.203"), 69);
		System.out.println(sendFile2);
		final int receiveFile = TFTPHelper.receiveFile("D:/Downloads/TO SEND/ss.txt", "sss.txt", InetAddress.getByName("134.214.119.203"), 69);
		System.out.println(receiveFile);
	}
}
