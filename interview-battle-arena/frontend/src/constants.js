export const CATEGORIES = [
  { value: 'JAVA', label: 'Java' },
  { value: 'SPRING_BOOT', label: 'Spring Boot' },
  { value: 'SQL', label: 'SQL' },
  { value: 'DSA', label: 'DSA' },
  { value: 'OOP', label: 'OOP' },
  { value: 'DBMS', label: 'DBMS' },
];

export const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD'];

export const categoryLabel = (value) =>
  CATEGORIES.find((c) => c.value === value)?.label ?? value;

export const difficultyLabel = (value) =>
  value.charAt(0) + value.slice(1).toLowerCase();