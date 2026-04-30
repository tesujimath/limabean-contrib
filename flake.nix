{
  description = "A development environment flake for limabean-contrib.";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";

    limabean = {
      url = "github:tesujimath/limabean";
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

            shellHook = ''
              # use local mods for now:
              export LIMABEAN_CLJ_LOCAL_ROOT="$(pwd)/../../tesujimath/limabean/clj"
              export LIMABEAN_LOG="$(pwd)/limabean.log"
              export LIMABEAN_POD_LOG="$(pwd)/limabean-pod.log"
              export LIMABEAN_DEBUG_DIR="$(pwd)/debug"
            '';
          };
        }
      );
}
