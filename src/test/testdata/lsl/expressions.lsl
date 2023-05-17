libsl "1.0.0";
library expressions;

typealias Int = int32;

automaton A : Int {
    var tmp: Int;

    fun f(param: Int) {
      tmp = 2 + 3;
      tmp = 2 + 3 + 4;
      tmp = (2 + 3) / 4;
      tmp = (2 + 3 + 4) / 5;
      tmp = 10 + 14 * (2 + 1);
      tmp = 10 + 14 / (2 + 1);
      tmp = 10 << 2;
      tmp = (10 << 2) + 14;
    }
}
