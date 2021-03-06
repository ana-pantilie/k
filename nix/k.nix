{ lib, mavenix, nix-gitignore, makeWrapper
, flex, gcc, git, gmp, jdk, mpfr, pkgconfig, python3, z3
}:

let inherit (nix-gitignore) gitignoreSourcePure; in

let
  hostInputs = [ flex gcc gmp jdk mpfr pkgconfig python3 z3 ];
  # PATH used at runtime
  hostPATH = lib.makeBinPath hostInputs;
in

mavenix.buildMaven {
  name = "k-5.0.0";
  infoFile = ./mavenix.lock;
  src =
    gitignoreSourcePure
      [
        ".git/" "result*" "nix/" "*.nix"
        "haskell-backend/src/main/native/haskell-backend/*"
        "!haskell-backend/src/main/native/haskell-backend/src"  # need prelude.kore
        "llvm-backend/src/main/native/llvm-backend/*"
        "!llvm-backend/src/main/native/llvm-backend/matching"  # need pom.xml
        "k-distribution/tests/regression-new"
      ]
      ./..;

  # Cannot enable unit tests until a bug is fixed upstream (in Mavenix).
  doCheck = false;

  # Add build dependencies
  #
  nativeBuildInputs = [ makeWrapper ];

  buildInputs =
    [ git ]
    ++ hostInputs;

  # Set build environment variables
  #
  MAVEN_OPTS = [
    "-DskipKTest=true"
    "-Dllvm.backend.skip=true"
    "-Dhaskell.backend.skip=true"
  ];

  # Attributes are passed to the underlying `stdenv.mkDerivation`, so build
  #   hooks can be set here also.
  #
  postPatch = ''
    patchShebangs k-distribution/src/main/scripts/bin
    patchShebangs k-distribution/src/main/scripts/lib
  '';

  postInstall = ''
    cp -r k-distribution/target/release/k/{bin,include,lib} $out/

    rm "$out/bin/k-configure-opam"
    rm "$out/bin/k-configure-opam-dev"
    rm "$out/bin/kserver-opam"
    rm -fr "$out/lib/opam"

    for prog in $out/bin/*; do
      wrapProgram $prog --prefix PATH : ${hostPATH}
    done

    prelude_kore="$out/include/kframework/kore/prelude.kore"
    if ! [[ -f "$prelude_kore" ]]
    then
        echo 1>&2 "missing output: $prelude_kore"
        exit 1
    fi
  '';

  # Add extra maven dependencies which might not have been picked up
  #   automatically
  #
  #deps = [
  #  { path = "org/group-id/artifactId/version/file.jar"; sha1 = "0123456789abcdef"; }
  #  { path = "org/group-id/artifactId/version/file.pom"; sha1 = "123456789abcdef0"; }
  #];

  # Add dependencies on other mavenix derivations
  #
  #drvs = [ (import ../other/mavenix/derivation {}) ];

  # Override which maven package to build with
  #
  #maven = maven.overrideAttrs (_: { jdk = pkgs.oraclejdk10; });

  # Override remote repository URLs and settings.xml
  #
  #remotes = { central = "https://repo.maven.apache.org/maven2"; };
  #settings = ./settings.xml;
}
