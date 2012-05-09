import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetToSerial {
	public static void main(String[] args) {
		ServerSocket serverSocket;
		SerialPort serialPort;

		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier("COM6");
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("serial in use");
				System.exit(1);
			}
			CommPort commPort = portIdentifier.open("NetToSerial", 2000);
			serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN
					| SerialPort.FLOWCONTROL_XONXOFF_OUT);
			System.out.println("Open COM port");

			serverSocket = new ServerSocket(4444, 1);

			System.out.println("Listener");
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Connecting client");
				new AtoB(serialPort.getInputStream(), socket.getOutputStream());
				new AtoB(socket.getInputStream(), serialPort.getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class AtoB implements Runnable {
		private InputStream in;
		private OutputStream out;

		public AtoB(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;

			Thread t = new Thread(this);
			System.out.println("started thread " + t.getName());
			t.start();
		}

		public void run() {
			byte[] buffer = new byte[1024];
			int len = -1;
			try {
				while ((len = this.in.read(buffer)) > -1) {
					System.out.write(buffer,0,len);
					out.write(buffer, 0, len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Ending thread "
					+ Thread.currentThread().getName());
		}
	}
}