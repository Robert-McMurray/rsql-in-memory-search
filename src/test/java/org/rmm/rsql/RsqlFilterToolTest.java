package org.rmm.rsql;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link RsqlFilterTool} amd sub logic.
 *
 * @author Rob McMurray
 */
class RsqlFilterToolTest {
    private final RsqlFilterTool tool = new RsqlFilterTool();

    @Test
    void simpleEquals() {
        List<TestObjectVO> objects = Lists.newArrayList(
                TestObjectVO.builder().field1("f1").field2(1).field3(true).build(),
                TestObjectVO.builder().field1("f2").field2(2).field3(false).build(),
                TestObjectVO.builder().field1("f3").field2(3).field3(true).build(),
                TestObjectVO.builder().field1(null).field2(null).field3(null).build()
        );

        String rsqlFilter = " field1==f1";

        List<TestObjectVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(1).containsOnly(objects.get(0));
    }

    @Test
    void simpleNotEquals() {
        List<TestObjectVO> objects = Lists.newArrayList(
                TestObjectVO.builder().field1("f1").field2(1).field3(true).build(),
                TestObjectVO.builder().field1("f2").field2(2).field3(false).build(),
                TestObjectVO.builder().field1("f3").field2(3).field3(true).build(),
                TestObjectVO.builder().field1(null).field2(null).field3(null).build()
        );

        String rsqlFilter = " field1!=f1";

        List<TestObjectVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(3).containsOnly(objects.get(1), objects.get(2), objects.get(3));
    }

    @Test
    void simpleIn() {
        List<TestObjectVO> objects = Lists.newArrayList(
                TestObjectVO.builder().field1("f1").field2(1).field3(true).build(),
                TestObjectVO.builder().field1("f2").field2(2).field3(false).build(),
                TestObjectVO.builder().field1("f3").field2(3).field3(true).build(),
                TestObjectVO.builder().field1(null).field2(null).field3(null).build()
        );

        String rsqlFilter = "field1=in=(f1,f2,f3)";

        List<TestObjectVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(3).containsOnly(objects.get(0), objects.get(1), objects.get(2));
    }

    @Test
    void compositeFilter() {
        List<TestObjectVO> objects = Lists.newArrayList(
                TestObjectVO.builder().field1("f1").field2(1).field3(true).build(),
                TestObjectVO.builder().field1("f2").field2(2).field3(false).build(),
                TestObjectVO.builder().field1("f3").field2(3).field3(true).build(),
                TestObjectVO.builder().field1(null).field2(null).field3(null).build()
        );

        String rsqlFilter = "field1==f1,(field2!=1;field3==false)";

        List<TestObjectVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(2).containsOnly(objects.get(0), objects.get(1));
    }

    @Test
    void simpleEqualsReferencingInheritedField() {
        List<TestObjectExtendOneVO> objects = Lists.newArrayList(
                TestObjectExtendOneVO.builder().field1("f1").field2(1).field3(true).field4("f4-1").build(),
                TestObjectExtendOneVO.builder().field1("f2").field2(2).field3(false).field4("f4-2").build(),
                TestObjectExtendOneVO.builder().field1("f3").field2(3).field3(true).field4("f4-3").build(),
                TestObjectExtendOneVO.builder().field1(null).field2(null).field3(null).field4(null).build()
        );

        String rsqlFilter = " field1==f1";

        List<TestObjectExtendOneVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(1).containsOnly(objects.get(0));
    }

    @Test
    void simpleEqualsWithChildClassesInList() {
        List<TestObjectVO> objects = Lists.newArrayList(
                TestObjectExtendOneVO.builder().field1("f1").field2(1).field3(true).field4("f4-1").build(),
                TestObjectExtendTwoVO.builder().field1("f2").field2(2).field3(false).field5("f5-1").build(),
                TestObjectVO.builder().field1("f3").field2(3).field3(true).build(),
                TestObjectExtendOneVO.builder().field1(null).field2(null).field3(null).field4(null).build()
        );

        String rsqlFilter = " field1==f1";

        List<TestObjectVO> result = tool.filter(objects, rsqlFilter);
        assertThat(result).isNotNull();
        assertThat(result.toArray()).isNotEmpty().hasSize(1).containsOnly(objects.get(0));
    }

    @Test
    void simpleLike() {
        List<TestObjectVO> objects = Lists.newArrayList();
        String rsqlFilter = "field1=like=f";
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, () -> tool.filter(objects, rsqlFilter));
        assertThat(ex.getMessage()).isEqualTo("Like logic not yet implemented");
    }
}