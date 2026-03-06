{
  description = "A development environment flake for limabean-contrib.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";

    limabean = {
      url = "github:tesujimath/limabean?ref=refs/tags/0.3.2";
      # url = "github:tesujimath/limabean";
      # url = "github:tesujimath/limabean?ref=refs/heads/plugin-contrib-support";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = inputs:
    inputs.flake-utils.lib.eachDefaultSystem
      (system:
        let
          pkgs = import inputs.nixpkgs {
            inherit system;
          };

          flakePkgs = {
            limabean = inputs.limabean.packages.${system}.default;
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
            nativeBuildInputs = [
              flakePkgs.limabean
            ] ++ ci-packages;
          };
        }
      );
}
