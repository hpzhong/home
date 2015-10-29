package sample;

public class Demo {

	public static void main(String[] args) {
		new Demo("Xiaofeng").myCall();
	}

	public Demo(){
		this.name = "default";
	}

	private String name = null;
	public Demo(String name){
		this.name = name;
	}

	public void myCall(){
		SubClass s = new SubClass(this.name);
		s.say();
	}
}
