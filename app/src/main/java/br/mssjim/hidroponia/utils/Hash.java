package br.mssjim.hidroponia.utils;

public class Hash {
    // TODO Executar função em Cloud

    public static String code(String s) {
        StringBuilder out = new StringBuilder();
        for(char c : s.toCharArray()) {
            switch(c) {
                case 'T':
                    out.append("P");
                    break;
                case 'E':
                    out.append("O");
                    break;
                case 'N':
                    out.append("L");
                    break;
                case 'I':
                    out.append("A");
                    break;
                case 'S':
                    out.append("R");
                    break;
                case 'P':
                    out.append("T");
                    break;
                case 'O':
                    out.append("E");
                    break;
                case 'L':
                    out.append("N");
                    break;
                case 'A':
                    out.append("I");
                    break;
                case 'R':
                    out.append("S");
                    break;

                case 't':
                    out.append("p");
                    break;
                case 'e':
                    out.append("o");
                    break;
                case 'n':
                    out.append("l");
                    break;
                case 'i':
                    out.append("a");
                    break;
                case 's':
                    out.append("r");
                    break;
                case 'p':
                    out.append("t");
                    break;
                case 'o':
                    out.append("e");
                    break;
                case 'l':
                    out.append("n");
                    break;
                case 'a':
                    out.append("i");
                    break;
                case 'r':
                    out.append("s");
                    break;

                case '1':
                    out.append("9");
                    break;
                case '2':
                    out.append("8");
                    break;
                case '3':
                    out.append("7");
                    break;
                case '4':
                    out.append("6");
                    break;
                case '5':
                    out.append("0");
                    break;
                case '6':
                    out.append("4");
                    break;
                case '7':
                    out.append("3");
                    break;
                case '8':
                    out.append("2");
                    break;
                case '9':
                    out.append("1");
                    break;
                case '0':
                    out.append("5");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append("RXUgcXVlcm8gY29tZXIgcGFvIGFnb3Jh==");
        return out.reverse().toString();
    }
}
