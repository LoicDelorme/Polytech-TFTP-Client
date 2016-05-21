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
		// final int sendFile = TFTPHelper.sendFile("D:/Downloads/ihm1_fx_all_4up.pdf", "ihm1_fx_all_4up.pdf", InetAddress.getByName("localhost"), 69);
		// System.out.println(sendFile);
		final int receiveFile = TFTPHelper.receiveFile("D:/Downloads/aaaaa.pdf", "ihm1_fx_all_4up.pdf", InetAddress.getByName("localhost"), 69);
		System.out.println(receiveFile);
	}
}
