package zjc.com.netty.pratice.bio;

public class TestCase {
	
	public static void main(String[] args) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				new Server().start();
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				new Client("tetst",30005).start();
			}
		}).start();
		
		
	}

}
