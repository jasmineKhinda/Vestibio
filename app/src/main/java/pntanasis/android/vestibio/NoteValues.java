package pntanasis.android.vestibio;

public enum NoteValues{

	four("4");

	private String noteValue;

	NoteValues(String noteValue) {
		this.noteValue = noteValue;
	}

	@Override
	public String toString() {
		return noteValue;
	}

	public short getNum() {
		return Short.parseShort(noteValue);
	}
}
