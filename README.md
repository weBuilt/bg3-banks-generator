# MaterialBank&TextureBank Generator for Baldur’s Gate 3

MaterialBank & TextureBank Generator is a Scala-based tool designed to automate the creation and regeneration of MaterialBank and TextureBank files for Baldur’s Gate 3 mods. The tool leverages existing MaterialBank and TextureBank files along with texture files to streamline the modding process.

## Key Features
Automated Generation: Automatically creates new MaterialBank and TextureBank files

Regeneration Support: Easily regenerates existing files with updated textures

File Compatibility: Works with both existing MaterialBank/TextureBank files and raw texture files

Modding Optimization: Simplifies the process of updating mod assets

## Installation
1. Clone the Repository:`git clone https://github.com/weBuilt/bg3-banks-generator.git`
2. Configure ```modSources textureBankOutput materialBankOutput```
3. Build the Project: `sbt compile`
4. Run the Application: `sbt run`

## Prerequisites:
Scala runtime environment

Java Development Kit (JDK) version 11 or higher

Baldur’s Gate 3 game files

Modding Tools: Basic understanding of BG3 modding

## To be implemented:
Out of project configuration

CLI interface

VisualBank Support