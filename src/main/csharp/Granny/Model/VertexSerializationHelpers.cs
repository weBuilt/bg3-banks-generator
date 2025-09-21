using gr2_parser.Granny.GR2;
using OpenTK.Mathematics;
using System.Reflection;
using System.Reflection.Emit;

namespace gr2_parser.Granny.Model;

public static class VertexSerializationHelpers
{
    public static Vector3 ReadVector3(GR2Reader reader)
    {
        Vector3 v;
        v.X = reader.Reader.ReadSingle();
        v.Y = reader.Reader.ReadSingle();
        v.Z = reader.Reader.ReadSingle();
        return v;
    }
    public static Vector3 ReadNormalSWordVector4As3(GR2Reader reader)
    {
        Vector3 v;
        v.X = reader.Reader.ReadInt16() / 32767.0f;
        v.Y = reader.Reader.ReadInt16() / 32767.0f;
        v.Z = reader.Reader.ReadInt16() / 32767.0f;
        reader.Reader.ReadInt16(); // Unused word
        return v;
    }
    public static BoneWeight ReadInfluences2(GR2Reader reader)
    {
        BoneWeight v;
        v.A = reader.Reader.ReadByte();
        v.B = reader.Reader.ReadByte();
        v.C = 0;
        v.D = 0;
        return v;
    }

    public static BoneWeight ReadInfluences(GR2Reader reader)
    {
        BoneWeight v;
        v.A = reader.Reader.ReadByte();
        v.B = reader.Reader.ReadByte();
        v.C = reader.Reader.ReadByte();
        v.D = reader.Reader.ReadByte();
        return v;
    }
    public static Vector3 ReadHalfVector4As3(GR2Reader reader)
    {
        Vector3 v;
        v.X = (float)reader.Reader.ReadHalf();
        v.Y = (float)reader.Reader.ReadHalf();
        v.Z = (float)reader.Reader.ReadHalf();
        reader.Reader.ReadUInt16();
        return v;
    }
    public static Vector3 ReadNormalSByteVector4As3(GR2Reader reader)
    {
        Vector3 v;
        v.X = reader.Reader.ReadSByte() / 127.0f;
        v.Y = reader.Reader.ReadSByte() / 127.0f;
        v.Z = reader.Reader.ReadSByte() / 127.0f;
        reader.Reader.ReadSByte(); // Unused byte
        return v;
    }
    public static Quaternion ReadBinormalShortVector4(GR2Reader reader)
    {
        return new Quaternion
        {
            X = reader.Reader.ReadInt16() / 32767.0f,
            Y = reader.Reader.ReadInt16() / 32767.0f,
            Z = reader.Reader.ReadInt16() / 32767.0f,
            W = reader.Reader.ReadInt16() / 32767.0f
        };
    }

    private static Matrix3 QTangentToMatrix(Quaternion q)
    {
        Matrix3 m = new Matrix3(
            1.0f - 2.0f * (q.Y * q.Y + q.Z * q.Z), 2 * (q.X * q.Y + q.W * q.Z), 2 * (q.X * q.Z - q.W * q.Y),
            2.0f * (q.X * q.Y - q.W * q.Z), 1 - 2 * (q.X * q.X + q.Z * q.Z), 2 * (q.Y * q.Z + q.W * q.X),
            0.0f, 0.0f, 0.0f
        );

        m.Row2 = Vector3.Cross(m.Row0, m.Row1) * ((q.W < 0.0f) ? -1.0f : 1.0f);
        return m;
    }
    public static Matrix3 ReadQTangent(GR2Reader reader)
    {
        Quaternion qTangent = ReadBinormalShortVector4(reader);
        return QTangentToMatrix(qTangent);
    }
    public static Vector4 ReadVector4(GR2Reader reader)
    {
        Vector4 v;
        v.X = reader.Reader.ReadSingle();
        v.Y = reader.Reader.ReadSingle();
        v.Z = reader.Reader.ReadSingle();
        v.W = reader.Reader.ReadSingle();
        return v;
    }
    public static Vector2 ReadVector2(GR2Reader reader)
    {
        Vector2 v;
        v.X = reader.Reader.ReadSingle();
        v.Y = reader.Reader.ReadSingle();
        return v;
    }

    public static Vector2 ReadHalfVector2(GR2Reader reader)
    {
        Vector2 v;
        v.X = (float)reader.Reader.ReadHalf();
        v.Y = (float)reader.Reader.ReadHalf();
        return v;
    }
    
    public static Vector4 ReadNormalByteVector4(GR2Reader reader)
    {
        Vector4 v;
        v.X = reader.Reader.ReadByte() / 255.0f;
        v.Y = reader.Reader.ReadByte() / 255.0f;
        v.Z = reader.Reader.ReadByte() / 255.0f;
        v.W = reader.Reader.ReadByte() / 255.0f;
        return v;
    }
    public static void Unserialize(GR2Reader reader, Vertex v)
    {
        var d = v.Format;

        switch (d.PositionType)
        {
            case PositionType.None: break;
            case PositionType.Float3: v.Position = ReadVector3(reader); break;
            case PositionType.Word4: v.Position = ReadNormalSWordVector4As3(reader); break;
        }

        if (d.HasBoneWeights)
        {
            if (d.NumBoneInfluences == 2)
            {
                v.BoneWeights = ReadInfluences2(reader);
                v.BoneIndices = ReadInfluences2(reader);
            }
            else
            {
                v.BoneWeights = ReadInfluences(reader);
                v.BoneIndices = ReadInfluences(reader);
            }
        }

        switch (d.NormalType)
        {
            case NormalType.None: break;
            case NormalType.Float3: v.Normal = ReadVector3(reader); break;
            case NormalType.Half4: v.Normal = ReadHalfVector4As3(reader); break;
            case NormalType.Byte4: v.Normal = ReadNormalSByteVector4As3(reader); break;
            case NormalType.QTangent:
                {
                    var qTangent = ReadQTangent(reader);
                    v.Normal = qTangent.Row2;
                    v.Tangent = qTangent.Row1;
                    v.Binormal = qTangent.Row0;
                    break;
                }
        }

        switch (d.TangentType)
        {
            case NormalType.None: break;
            case NormalType.Float3: v.Tangent = ReadVector3(reader); break;
            case NormalType.Half4: v.Tangent = ReadHalfVector4As3(reader); break;
            case NormalType.Byte4: v.Tangent = ReadNormalSByteVector4As3(reader); break;
            case NormalType.QTangent: break; // Tangent read from QTangent
        }

        switch (d.BinormalType)
        {
            case NormalType.None: break;
            case NormalType.Float3: v.Binormal = ReadVector3(reader); break;
            case NormalType.Half4: v.Binormal = ReadHalfVector4As3(reader); break;
            case NormalType.Byte4: v.Binormal = ReadNormalSByteVector4As3(reader); break;
            case NormalType.QTangent: break; // Binormal read from QTangent
        }

        if (d.ColorMaps > 0)
        {
            for (var i = 0; i < d.ColorMaps; i++)
            {
                var color = d.ColorMapType switch
                {
                    ColorMapType.Float4 => ReadVector4(reader),
                    ColorMapType.Byte4 => ReadNormalByteVector4(reader),
                    _ => throw new Exception($"Cannot unserialize color map: Unsupported format {d.ColorMapType}"),
                };
                v.SetColor(i, color);
            }
        }

        if (d.TextureCoordinates > 0)
        {
            for (var i = 0; i < d.TextureCoordinates; i++)
            {
                var uv = d.TextureCoordinateType switch
                {
                    TextureCoordinateType.Float2 => ReadVector2(reader),
                    TextureCoordinateType.Half2 => ReadHalfVector2(reader),
                    _ => throw new Exception($"Cannot unserialize UV map: Unsupported format {d.TextureCoordinateType}"),
                };
                v.SetUV(i, uv);
            }
        }
    }
};


public static class VertexTypeBuilder
{
    private static ModuleBuilder ModBuilder;

    private static ModuleBuilder GetModuleBuilder()
    {
        if (ModBuilder != null)
        {
            return ModBuilder;
        }

        var an = new AssemblyName("VertexFactoryAssembly");
        var assemblyBuilder = AssemblyBuilder.DefineDynamicAssembly(an, AssemblyBuilderAccess.Run);
        var moduleBuilder = assemblyBuilder.DefineDynamicModule("VertexFactoryClasses");
        ModBuilder = moduleBuilder;
        return ModBuilder;
    }

    public static Type CreateVertexSubtype(string className)
    {
        var cls = GetModuleBuilder().GetType(className);
        if (cls != null)
        {
            return cls;
        }

        TypeBuilder tb = GetModuleBuilder().DefineType(className,
                TypeAttributes.Public |
                TypeAttributes.Class |
                TypeAttributes.AutoClass |
                TypeAttributes.AnsiClass |
                TypeAttributes.BeforeFieldInit |
                TypeAttributes.AutoLayout,
                null);
        ConstructorBuilder constructor = tb.DefineDefaultConstructor(
            MethodAttributes.Public | 
            MethodAttributes.SpecialName | 
            MethodAttributes.RTSpecialName);

        tb.SetParent(typeof(Vertex));

        return tb.CreateType();
    }
}


class VertexDefinitionSelector : StructDefinitionSelector
{
    private void AddMember(StructDefinition defn, String name, MemberType type, UInt32 arraySize)
    {
        var member = new MemberDefinition
        {
            Type = type,
            Name = name,
            GrannyName = name,
            Definition = null,
            ArraySize = arraySize,
            Extra = [0, 0, 0],
            Unknown = 0
        };
        defn.Members.Add(member);
    }

    public StructDefinition CreateStructDefinition(object instance)
    {
        var desc = (instance as Vertex).Format;
        var defn = new StructDefinition
        {
            Members = [],
            MixedMarshal = true,
            Type = typeof(Vertex)
        };

        switch (desc.PositionType)
        {
            case PositionType.None: break;
            case PositionType.Float3: AddMember(defn, "Position", MemberType.Real32, 3); break;
            case PositionType.Word4: AddMember(defn, "Position", MemberType.BinormalInt16, 4); break;
        }

        if (desc.HasBoneWeights)
        {
            AddMember(defn, "BoneWeights", MemberType.NormalUInt8, (UInt32)desc.NumBoneInfluences);
            AddMember(defn, "BoneIndices", MemberType.UInt8, (UInt32)desc.NumBoneInfluences);
        }

        switch (desc.NormalType)
        {
            case NormalType.None: break;
            case NormalType.Float3: AddMember(defn, "Normal", MemberType.Real32, 3); break;
            case NormalType.Half4: AddMember(defn, "Normal", MemberType.Real16, 4); break;
            case NormalType.Byte4: AddMember(defn, "Normal", MemberType.BinormalInt8, 4); break;
            case NormalType.QTangent: AddMember(defn, "QTangent", MemberType.BinormalInt16, 4); break;
        }

        switch (desc.TangentType)
        {
            case NormalType.None: break;
            case NormalType.Float3: AddMember(defn, "Tangent", MemberType.Real32, 3); break;
            case NormalType.Half4: AddMember(defn, "Tangent", MemberType.Real16, 4); break;
            case NormalType.Byte4: AddMember(defn, "Tangent", MemberType.BinormalInt8, 4); break;
            case NormalType.QTangent: break; // Tangent saved into QTangent
        }

        switch (desc.BinormalType)
        {
            case NormalType.None: break;
            case NormalType.Float3: AddMember(defn, "Binormal", MemberType.Real32, 3); break;
            case NormalType.Half4: AddMember(defn, "Binormal", MemberType.Real16, 4); break;
            case NormalType.Byte4: AddMember(defn, "Binormal", MemberType.BinormalInt8, 4); break;
            case NormalType.QTangent: break; // Binormal saved into QTangent
        }

        for (int i = 0; i < desc.ColorMaps; i++)
        {
            switch (desc.ColorMapType)
            {
                case ColorMapType.Float4: AddMember(defn, "DiffuseColor" + i.ToString(), MemberType.Real32, 4); break;
                case ColorMapType.Byte4: AddMember(defn, "DiffuseColor" + i.ToString(), MemberType.NormalUInt8, 4); break;
            }
        }

        for (int i = 0; i < desc.TextureCoordinates; i++)
        {
            switch (desc.TextureCoordinateType)
            {
                case TextureCoordinateType.Float2: AddMember(defn, "TextureCoordinates" + i.ToString(), MemberType.Real32, 2); break;
                case TextureCoordinateType.Half2: AddMember(defn, "TextureCoordinates" + i.ToString(), MemberType.Real16, 2); break;
            }
        }

        return defn;
    }
}
