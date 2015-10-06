package pntanasis.android.metronome;

/**
 * Created by felixdollack on 06.10.15.
 */
public final class MidiNotes {

    public static double frequency( byte midi ) {
        return Math.pow( 2, (midi-69)/12 )* 440;
    }

    public static double midi( double f ) {
        return 12*Math.log( f/440 )+69;
    }

    // midi notes from a piano
    public static final byte A0 = 21;
    public static final byte AisBb0 = 22;
    public static final byte B0 = 23;

    public static final byte C1 = 24;
    public static final byte CisDb1 = 25;
    public static final byte D1 = 26;
    public static final byte DisEb1 = 27;
    public static final byte E1 = 28;
    public static final byte F1 = 29;
    public static final byte FisGb1 = 30;
    public static final byte G1 = 31;
    public static final byte GisAb1 = 32;
    public static final byte A1 = 33;
    public static final byte AisBb1 = 34;
    public static final byte B1 = 35;

    public static final byte C2 = 36;
    public static final byte CisDb2 = 37;
    public static final byte D2 = 38;
    public static final byte DisEb2 = 39;
    public static final byte E2 = 40;
    public static final byte F2 = 41;
    public static final byte FisGb2 = 42;
    public static final byte G2 = 43;
    public static final byte GisAb2 = 44;
    public static final byte A2 = 45;
    public static final byte AisBb2 = 46;
    public static final byte B2 = 47;

    public static final byte C3 = 48;
    public static final byte CisDb3 = 49;
    public static final byte D3 = 50;
    public static final byte DisEb3 = 51;
    public static final byte E3 = 52;
    public static final byte F3 = 53;
    public static final byte FisGb3 = 54;
    public static final byte G3 = 55;
    public static final byte GisAb3 = 56;
    public static final byte A3 = 57;
    public static final byte AisBb3 = 58;
    public static final byte B3 = 59;

    public static final byte C4 = 60;
    public static final byte CisDb4 = 61;
    public static final byte D4 = 62;
    public static final byte DisEb4 = 63;
    public static final byte E4 = 64;
    public static final byte F4 = 65;
    public static final byte FisGb4 = 66;
    public static final byte G4 = 67;
    public static final byte GisAb4 = 68;
    public static final byte A4 = 69;
    public static final byte AisBb4 = 70;
    public static final byte B4 = 71;

    public static final byte C5 = 72;
    public static final byte CisDb5 = 73;
    public static final byte D5 = 74;
    public static final byte DisEb5 = 75;
    public static final byte E5 = 76;
    public static final byte F5 = 77;
    public static final byte FisGb5 = 78;
    public static final byte G5 = 79;
    public static final byte GisAb5 = 80;
    public static final byte A5 = 81;
    public static final byte AisBb5 = 82;
    public static final byte B5 = 83;

    public static final byte C6 = 84;
    public static final byte CisDb6 = 85;
    public static final byte D6 = 86;
    public static final byte DisEb6 = 87;
    public static final byte E6 = 88;
    public static final byte F6 = 89;
    public static final byte FisGb6 = 90;
    public static final byte G6 = 91;
    public static final byte GisAb6 = 92;
    public static final byte A6 = 93;
    public static final byte AisBb6 = 94;
    public static final byte B6 = 95;

    public static final byte C7 = 96;
    public static final byte CisDb7 = 97;
    public static final byte D7 = 98;
    public static final byte DisEb7 = 99;
    public static final byte E7 = 100;
    public static final byte F7 = 101;
    public static final byte FisGb7 = 102;
    public static final byte G7 = 103;
    public static final byte GisAb7 = 104;
    public static final byte A7 = 105;
    public static final byte AisBb7 = 106;
    public static final byte B7 = 107;

    public static final byte C8 = 108;
}
