using OpenTK.Mathematics;
using gr2_parser.Granny.GR2;

namespace gr2_parser.Granny.Model;

public class Deduplicator<T>(IEqualityComparer<T> comparer)
{
    private readonly IEqualityComparer<T> Comparer = comparer;
    public Dictionary<int, int> DeduplicationMap = [];
    public List<T> Uniques = [];

    public void MakeIdentityMapping(IEnumerable<T> items)
    {
        var i = 0;
        foreach (var item in items)
        {
            Uniques.Add(item);
            DeduplicationMap.Add(i, i);
            i++;
        }
    }

    public void Deduplicate(IEnumerable<T> items)
    {
        var uniqueItems = new Dictionary<T, int>(Comparer);
        var i = 0;
        foreach (var item in items)
        {
            if (!uniqueItems.TryGetValue(item, out int mappedIndex))
            {
                mappedIndex = uniqueItems.Count;
                uniqueItems.Add(item, mappedIndex);
                Uniques.Add(item);
            }

            DeduplicationMap.Add(i, mappedIndex);
            i++;
        }
    }
}

class GenericEqualityComparer<T> : IEqualityComparer<T> where T : IEquatable<T>
{
    public bool Equals(T a, T b)
    {
        return a.Equals(b);
    }

    public int GetHashCode(T v)
    {
        return v.GetHashCode();
    }
}

public struct SkinnedVertex : IEquatable<SkinnedVertex>
{
    public Vector3 Position;
    public BoneWeight Indices;
    public BoneWeight Weights;

    public bool Equals(SkinnedVertex w)
    {
        return Position.Equals(w.Position)
            && Indices.Equals(w.Indices)
            && Weights.Equals(w.Weights);
    }

    public override int GetHashCode()
    {
        return Position.GetHashCode() ^ Indices.GetHashCode() ^ Weights.GetHashCode();
    }
}


public class VertexAnnotationSet
{
    public string Name;
    [Serialization(Type = MemberType.ReferenceToVariantArray)]
    public List<object> VertexAnnotations;
    public Int32 IndicesMapFromVertexToAnnotation;
    public List<TriIndex> VertexAnnotationIndices;
}

public class VertexDataSectionSelector : SectionSelector
{
    public SectionType SelectSection(MemberDefinition member, Type type, object obj)
    {
        if (obj is VertexData data)
        {
            return data.SerializationSection;
        }
        else
        {
            return SectionType.Invalid;
        }
    }
}

public class VertexData
{
    [Serialization(Type = MemberType.ReferenceToVariantArray, MixedMarshal = true,
        TypeSelector = typeof(VertexSerializer), Serializer = typeof(VertexSerializer),
        Kind = SerializationKind.UserMember)]
    public List<Vertex> Vertices;
    public List<GrannyString> VertexComponentNames;
    public List<VertexAnnotationSet> VertexAnnotationSets;

    [Serialization(Kind = SerializationKind.None)]
    public SectionType SerializationSection = SectionType.Invalid;

    public void PostLoad()
    {
        // Fix missing vertex component names
        if (VertexComponentNames == null)
        {
            VertexComponentNames = [];
            if (Vertices.Count > 0)
            {
                var components = Vertices[0].Format.ComponentNames();
                foreach (var name in components)
                {
                    VertexComponentNames.Add(new GrannyString(name));
                }
            }
        }
    }

}

public class TriTopologyGroup
{
    public int MaterialIndex;
    public int TriFirst;
    public int TriCount;
}

public class TriIndex
{
    public Int32 Int32;
}

public class TriIndex16
{
    public Int16 Int16;
}

public class TriAnnotationSet
{
    public string Name;
    [Serialization(Type = MemberType.ReferenceToVariantArray)]
    public object TriAnnotations;
    public Int32 IndicesMapFromTriToAnnotation;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> TriAnnotationIndices;
}

public class TriTopologySectionSelector : SectionSelector
{
    public SectionType SelectSection(MemberDefinition member, Type type, object obj)
    {
        if (obj is TriTopology)
        {
            return ((TriTopology)obj).SerializationSection;
        }
        else
        {
            return SectionType.Invalid;
        }
}
}

public class TriTopology
{
    public List<TriTopologyGroup> Groups;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> Indices;
    [Serialization(Prototype = typeof(TriIndex16), Kind = SerializationKind.UserMember, Serializer = typeof(UInt16ListSerializer))]
    public List<UInt16> Indices16;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> VertexToVertexMap;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> VertexToTriangleMap;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> SideToNeighborMap;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer), MinVersion = 0x80000038)]
    public List<Int32> PolygonIndexStarts;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer), MinVersion = 0x80000038)]
    public List<Int32> PolygonIndices;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> BonesForTriangle;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> TriangleToBoneIndices;
    public List<TriAnnotationSet> TriAnnotationSets;

    [Serialization(Kind = SerializationKind.None)]
    public SectionType SerializationSection = SectionType.Invalid;

    public void PostLoad()
    {
        // Convert 16-bit vertex indices to 32-bit indices
        // (for convenience, so we won't have to handle both Indices and Indices16 in all code paths)
        if (Indices16 != null)
        {
            Indices = new List<Int32>(Indices16.Count);
            foreach (var index in Indices16)
            {
                Indices.Add(index);
            }

            Indices16 = null;
        }
    }
}

public class BoneBinding
{
    public string BoneName;
    [Serialization(ArraySize = 3)]
    public float[] OBBMin;
    [Serialization(ArraySize = 3)]
    public float[] OBBMax;
    [Serialization(Prototype = typeof(TriIndex), Kind = SerializationKind.UserMember, Serializer = typeof(Int32ListSerializer))]
    public List<Int32> TriangleIndices;
}

public class MaterialReference
{
    public string Usage;
    public Material Map;
}

public class TextureLayout
{
    public Int32 BytesPerPixel;
    [Serialization(ArraySize = 4)]
    public Int32[] ShiftForComponent;
    [Serialization(ArraySize = 4)]
    public Int32[] BitsForComponent;
}

public class PixelByte
{
    public Byte UInt8;
}

public class TextureMipLevel
{
    public Int32 Stride;
    public List<PixelByte> PixelBytes;
}

public class TextureImage
{
    public List<TextureMipLevel> MIPLevels;
}

public class Texture
{
    public string FromFileName;
    public Int32 TextureType;
    public Int32 Width;
    public Int32 Height;
    public Int32 Encoding;
    public Int32 SubFormat;
    [Serialization(Type = MemberType.Inline)]
    public TextureLayout Layout;
    public List<TextureImage> Images;
    public object ExtendedData;
}

public class Material
{
    public string Name;
    public List<MaterialReference> Maps;
    public Texture Texture;
    public object ExtendedData;
}

public class MaterialBinding
{
    public Material Material;
}

public class MorphTarget
{
    public string ScalarName;
    [Serialization(SectionSelector = typeof(VertexDataSectionSelector))]
    public VertexData VertexData;
    public Int32 DataIsDeltas;
}

public class InfluencingJoints
{
    public List<int> BindJoints;
    public List<int> SkeletonJoints;
    public int[] BindRemaps;

    public static int[] BindJointsToRemaps(List<int> joints)
    {
        var maxJoint = joints.Count > 0 ? joints.Max() + 1 : 0;
        var remaps = new int[maxJoint];
        var i = 0;

        foreach (var joint in joints)
        {
            remaps[joint] = i++;
        }

        return remaps;
    }
}

public class Mesh
{
    public string Name;
    [Serialization(SectionSelector = typeof(VertexDataSectionSelector))]
    public VertexData PrimaryVertexData;
    public List<MorphTarget> MorphTargets;
    [Serialization(SectionSelector = typeof(TriTopologySectionSelector))]
    public TriTopology PrimaryTopology;
    [Serialization(DataArea = true)]
    public List<MaterialBinding> MaterialBindings;
    public List<BoneBinding> BoneBindings;
    [Serialization(Type = MemberType.VariantReference)]
    public DivinityMeshExtendedData ExtendedData;

    [Serialization(Kind = SerializationKind.None)]
    public Dictionary<int, List<int>> OriginalToConsolidatedVertexIndexMap;

    [Serialization(Kind = SerializationKind.None)]
    public VertexDescriptor VertexFormat;

    [Serialization(Kind = SerializationKind.None)]
    public int ExportOrder = -1;

    public void PostLoad()
    {
        if (PrimaryVertexData.Vertices.Count > 0)
        {
            VertexFormat = PrimaryVertexData.Vertices[0].Format;
        }

        if (ExtendedData != null
            && ExtendedData.UserMeshProperties != null
            && ExtendedData.UserMeshProperties.Flags[0] == 0
            && ExtendedData.UserMeshProperties.NewlyAdded)
        {
            ExtendedData.UserMeshProperties.MeshFlags = AutodetectMeshFlags();
        }
    }
    public DivinityModelFlag AutodetectMeshFlags()
    {
        DivinityModelFlag flags = 0;

        if (ExtendedData != null 
            && ExtendedData.UserMeshProperties != null
            && ExtendedData.UserMeshProperties.MeshFlags != 0)
        {
            return ExtendedData.UserMeshProperties.MeshFlags;
        }

        if (ExtendedData != null 
            && ExtendedData.UserDefinedProperties != null)
        {
            flags = UserDefinedPropertiesHelpers.UserDefinedPropertiesToMeshType(ExtendedData.UserDefinedProperties);
        }
        else
        {
            // Only mark model as cloth if it has colored vertices
            if (VertexFormat.ColorMaps > 0)
            {
                flags |= DivinityModelFlag.Cloth;
            }

            if (!VertexFormat.HasBoneWeights)
            {
                flags |= DivinityModelFlag.Rigid;
            }
        }

        return flags;
    }

    public List<string> VertexComponentNames()
    {
        if (PrimaryVertexData.VertexComponentNames != null
            && PrimaryVertexData.VertexComponentNames.Count > 0
            && PrimaryVertexData.VertexComponentNames[0].String != "")
        {
            return PrimaryVertexData.VertexComponentNames.Select(s => s.String).ToList();
        }
        else if (PrimaryVertexData.Vertices != null
            && PrimaryVertexData.Vertices.Count > 0)
        {
            return PrimaryVertexData.Vertices[0].Format.ComponentNames();
        }
        else
        {
            throw new ParsingException("Unable to determine mesh component list: No vertices and vertex component names available.");
        }
    }

    public bool IsSkinned()
    {
        // Check if we have both the BoneWeights and BoneIndices vertex components.
        bool hasWeights = false, hasIndices = false;

        // If we have vertices, check the vertex prototype, as VertexComponentNames is unreliable.
        if (PrimaryVertexData.Vertices.Count > 0)
        {
            var desc = PrimaryVertexData.Vertices[0].Format;
            hasWeights = hasIndices = desc.HasBoneWeights;
        }
        else
        {
            // Otherwise try to figure out the components from VertexComponentNames
            foreach (var component in PrimaryVertexData.VertexComponentNames)
            {
                if (component.String == "BoneWeights")
                    hasWeights = true;
                else if (component.String == "BoneIndices")
                    hasIndices = true;
            }
        }

        return hasWeights && hasIndices;
    }

    public InfluencingJoints GetInfluencingJoints(Skeleton skeleton)
    {
        HashSet<int> joints = [];

        foreach (var vert in PrimaryVertexData.Vertices)
        {
            if (vert.BoneWeights.A > 0) joints.Add(vert.BoneIndices.A);
            if (vert.BoneWeights.B > 0) joints.Add(vert.BoneIndices.B);
            if (vert.BoneWeights.C > 0) joints.Add(vert.BoneIndices.C);
            if (vert.BoneWeights.D > 0) joints.Add(vert.BoneIndices.D);
        }

        var ij = new InfluencingJoints();
        ij.BindJoints = joints.Order().ToList();
        ij.SkeletonJoints = [];
        foreach (var bindIndex in ij.BindJoints)
        {
            var binding = BoneBindings[bindIndex].BoneName;
            var jointIndex = skeleton.Bones.FindIndex((bone) => bone.Name == binding);
            if (jointIndex == -1)
            {
                throw new ParsingException($"Couldn't find bind bone {binding} in parent skeleton.");
            }

            ij.SkeletonJoints.Add(jointIndex);
        }

        ij.BindRemaps = InfluencingJoints.BindJointsToRemaps(ij.BindJoints);
        return ij;
    }

    public Tuple<Vector3, Vector3> CalculateOBB()
    {
        if (PrimaryVertexData.Vertices.Count == 0)
        {
            throw new ParsingException("Cannot calculate OBB for mesh with no vertices!");
        }
        
        var min = new Vector3(9999999.0f, 9999999.0f, 9999999.0f);
        var max = new Vector3(-9999999.0f, -9999999.0f, -9999999.0f);

        foreach (var vert in PrimaryVertexData.Vertices)
        {
            min.X = Math.Min(vert.Position.X, min.X);
            max.X = Math.Max(vert.Position.X, max.X);
            min.Y = Math.Min(vert.Position.Y, min.Y);
            max.Y = Math.Max(vert.Position.Y, max.Y);
            min.Z = Math.Min(vert.Position.Z, min.Z);
            max.Z = Math.Max(vert.Position.Z, max.Z);
        }

        return new Tuple<Vector3, Vector3>(min, max);
    }
}

