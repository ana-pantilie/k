// Copyright (c) 2020 K Team. All Rights Reserved.

module A-PASS-SPEC

imports A

claim <k> zoo(foo(X)) => koo(bar(X)) ... </k>
    requires X >Int 0

endmodule
