using gr2_parser.Granny.GR2;
using OpenTK.Mathematics;

namespace gr2_parser.Granny.Model;

public class Root
{
    [Serialization(Section = SectionType.Skeleton, Type = MemberType.ArrayOfReferences)]
    public List<Skeleton> Skeletons;
    [Serialization(Type = MemberType.ArrayOfReferences, SectionSelector = typeof(VertexDataSectionSelector))]
    public List<VertexData> VertexDatas;
    [Serialization(Type = MemberType.ArrayOfReferences, SectionSelector = typeof(TriTopologySectionSelector))]
    public List<TriTopology> TriTopologies;
    [Serialization(Section = SectionType.Mesh, Type = MemberType.ArrayOfReferences)]
    public List<Mesh> Meshes;
    [Serialization(Type = MemberType.ArrayOfReferences)]
    public List<Model> Models;
    // [Serialization(Type = MemberType.ArrayOfReferences)]
    // public List<Animation> Animations;
    [Serialization(Kind = SerializationKind.None)]
    public bool ZUp = false;
    [Serialization(Kind = SerializationKind.None)]
    public UInt32 GR2Tag;

    public static Root CreateEmpty()
    {
        return new Root
        {
            Skeletons = [],
            VertexDatas = [],
            TriTopologies = [],
            Meshes = [],
            Models = []
            // Animations = []
        };
    }

    public void PostLoad(UInt32 tag)
    {
        GR2Tag = tag;

        foreach (var vertexData in VertexDatas ?? Enumerable.Empty<VertexData>())
        {
            vertexData.PostLoad();
        }

        foreach (var triTopology in TriTopologies ?? Enumerable.Empty<TriTopology>())
        {
            triTopology.PostLoad();
        }

        Meshes?.ForEach(m => m.PostLoad());

        var modelIndex = 0;
        foreach (var model in Models ?? Enumerable.Empty<Model>())
        {
            foreach (var binding in model.MeshBindings ?? Enumerable.Empty<MeshBinding>())
            {
                binding.Mesh.ExportOrder = modelIndex++;
            }
        }

        foreach (var skeleton in Skeletons ?? Enumerable.Empty<Skeleton>())
        {
            skeleton.PostLoad(this);
        }
    }
}
