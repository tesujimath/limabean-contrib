{
  description = "A development environment flake for limabean-contrib.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = inputs:
    inputs.flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import inputs.nixpkgs {
            inherit system;
          };

          ci-packages = with pkgs; [
            bashInteractive
            coreutils
            diffutils
            just

            clojure
            git
          ];

        in
        with pkgs;
        {
          devShells.default = mkShell {
            nativeBuildInputs = ci-packages;
          };
        }
      );
}
