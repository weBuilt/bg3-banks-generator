// See https://aka.ms/new-console-template for more information
using System.Formats.Asn1;
using gr2_parser.Granny.GR2;
using gr2_parser.Granny.Model;

if (args == null | args.Length == 0)
{
    Console.WriteLine("ERR: No Filename");
    return;
}

var filename = args[0];
if (!filename.ToLower().EndsWith(".gr2"))
{
    Console.WriteLine("ERR: Not a GR2 File");
    return;
}

if (!File.Exists(filename))
{
    Console.WriteLine("ERR: File not found");
    return;
}
try
{
    using var fs = File.Open(args[0], FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
    var root = new Root();
    var gr2 = new GR2Reader(fs);
    gr2.Read(root);
    root.PostLoad(gr2.Tag);
    foreach (var skeleton in root.Skeletons)
    {
        Console.WriteLine($"template={skeleton.Name}");
    }
    foreach (var mesh in root.Meshes)
    {
        Console.WriteLine($"name={mesh.Name};order={mesh.ExportOrder};lod={mesh.ExtendedData.LOD}");
    }
}
catch (Exception ex)
{
    Console.WriteLine($"ERR: {ex.Message}");
}
