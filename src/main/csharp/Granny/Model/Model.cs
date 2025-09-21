using gr2_parser.Granny.GR2;

namespace gr2_parser.Granny.Model;

public class MeshBinding
{
    public Mesh Mesh;
}

public class Model
{
    public string Name;
    public Skeleton Skeleton;
    public Transform InitialPlacement;
    [Serialization(DataArea = true)]
    public List<MeshBinding> MeshBindings;
    [Serialization(Type = MemberType.VariantReference, MinVersion = 0x80000027)]
    public object ExtendedData;
}
