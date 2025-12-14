const delay = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

export interface VersionDiff {
  section: string;
  type: 'added' | 'removed' | 'modified';
  oldContent?: string;
  newContent?: string;
}

export interface AIComparisonResult {
  summary: string;
  changes: VersionDiff[];
  impactLevel: 'low' | 'medium' | 'high';
}

export const aiService = {
  // Mock AI version comparison
  compareVersions: async (_syllabusId: string, _oldVersionId: string): Promise<AIComparisonResult> => {
    await delay(1500);

    // Mock AI-generated diff
    return {
      summary: 'Phát hiện 5 thay đổi quan trọng: cập nhật CLO2, thêm phương pháp đánh giá mới, và điều chỉnh tài liệu tham khảo.',
      impactLevel: 'medium',
      changes: [
        {
          section: 'CLO2 - Mô tả',
          type: 'modified',
          oldContent: 'Sinh viên có khả năng phân tích các vấn đề cơ bản',
          newContent: 'Sinh viên có khả năng phân tích và giải quyết các vấn đề phức tạp trong lập trình',
        },
        {
          section: 'Phương pháp đánh giá',
          type: 'added',
          newContent: 'Bài tập nhóm - Trọng số 15%',
        },
        {
          section: 'Tài liệu tham khảo',
          type: 'added',
          newContent: 'Clean Code: A Handbook of Agile Software Craftsmanship - Robert C. Martin (2023)',
        },
        {
          section: 'Giờ tự học',
          type: 'modified',
          oldContent: '60 giờ',
          newContent: '75 giờ',
        },
        {
          section: 'Điều kiện tiên quyết',
          type: 'removed',
          oldContent: 'Hoàn thành môn Toán rời rạc',
        },
      ],
    };
  },

  // Mock AI generate syllabus summary
  generateSummary: async (_syllabusId: string): Promise<string> => {
    await delay(1200);

    return `Môn học này trang bị cho sinh viên kiến thức nền tảng về lập trình hướng đối tượng, 
bao gồm các khái niệm về lớp, đối tượng, kế thừa, đa hình, và trừu tượng hóa. 
Sinh viên sẽ thực hành qua 4 bài tập lớn và 1 dự án cuối kỳ. 
Phương pháp đánh giá bao gồm: Thi giữa kỳ (30%), Bài tập (30%), và Dự án (40%).`;
  },

  // Mock AI CLO-PLO mapping suggestion
  suggestCLOPLOMapping: async (cloDescription: string): Promise<string[]> => {
    await delay(800);

    // Mock PLO suggestions based on CLO description
    const keywords = cloDescription.toLowerCase();
    const suggestions: string[] = [];

    if (keywords.includes('phân tích') || keywords.includes('giải quyết')) {
      suggestions.push('PLO2 - Kỹ năng giải quyết vấn đề');
    }
    if (keywords.includes('thiết kế') || keywords.includes('xây dựng')) {
      suggestions.push('PLO3 - Kỹ năng thiết kế hệ thống');
    }
    if (keywords.includes('làm việc nhóm') || keywords.includes('giao tiếp')) {
      suggestions.push('PLO5 - Kỹ năng làm việc nhóm');
    }
    if (keywords.includes('nghiên cứu') || keywords.includes('tài liệu')) {
      suggestions.push('PLO4 - Năng lực tự học');
    }
    if (suggestions.length === 0) {
      suggestions.push('PLO1 - Kiến thức nền tảng ngành');
    }

    return suggestions;
  },

  // Mock AI PLO compliance check
  checkPLOCompliance: async (_syllabusId: string): Promise<{
    compliant: boolean;
    issues: Array<{ plo: string; message: string; severity: 'error' | 'warning' }>;
    suggestions: string[];
  }> => {
    await delay(1000);

    return {
      compliant: false,
      issues: [
        {
          plo: 'PLO2',
          message: 'CLO chưa ánh xạ đủ sang PLO2 (yêu cầu tối thiểu 2 CLO)',
          severity: 'error',
        },
        {
          plo: 'PLO5',
          message: 'Thiếu đánh giá kỹ năng làm việc nhóm cho PLO5',
          severity: 'warning',
        },
      ],
      suggestions: [
        'Thêm CLO về kỹ năng phân tích để đáp ứng PLO2',
        'Bổ sung phương pháp đánh giá nhóm (weight 10-15%) cho PLO5',
        'Xem xét tăng trọng số cho CLO ánh xạ sang PLO2 lên ít nhất 30%',
      ],
    };
  },
};
