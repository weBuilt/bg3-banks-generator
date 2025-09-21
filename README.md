# Banks Generator for Baldur’s Gate 3 Modding

MaterialBank, TextureBank and VisualBank Generator is a tool designed to automate the creation and regeneration of Bank files for Baldur’s Gate 3 mods.

## Key Features
Automated Generation: Automatically creates new Bank files

Regeneration Support: Easily regenerates existing files with updated textures and visuals

File Compatibility: Works with both existing Bank files and raw DDS and GR2 files

Modding Optimization: Simplifies the process of updating mod assets

## Build:
1. Clone the Repository:`git clone https://github.com/weBuilt/bg3-banks-generator.git`
2. Build dotnet app ```dotnet build "src/main/csharp/gr2-parser.csproj" -c Release -o "deploy/bin"```
3. Build scala app ```sbt assembly```
4. Configure your project in cfg file like `config-example.cfg`
5. Run your app ```java -jar deploy/lib/bg3-banks-gen.jar -conf config-example.cfg```

## Build Prerequisites:
Scala runtime environment

Java Development Kit (JDK) version 11 or higher

.Net 9+

Baldur’s Gate 3 game files

Modding Tools: Basic understanding of BG3 modding

## Version 2 plans:
- Mod initialization
- Hotload enable/disable
- Icons support
- UI so modding person don't interact with xml directly