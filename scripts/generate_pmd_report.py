import os
from pathlib import Path
import xml.etree.ElementTree as ET
from collections import defaultdict
from html import escape

BASE_DIR = Path(__file__).resolve().parent.parent
REPORT_DIR = BASE_DIR / "build/reports/pmd"
OUTPUT_FILE = REPORT_DIR / "pmd_report_grouped.html"

def parse_pmd_xml(file_path):
    issues = []
    if not file_path.exists():
        print(f"❌ File not found: {file_path}")
        return issues

    print(f"🔍 Parsing XML: {file_path}")
    tree = ET.parse(file_path)
    root = tree.getroot()

    namespace = ''
    if '}' in root.tag:
        namespace = root.tag.split('}')[0] + '}'

    for file_elem in root.findall(f"{namespace}file"):
        filename = Path(file_elem.attrib.get("name")).relative_to(BASE_DIR)
        for violation in file_elem.findall(f"{namespace}violation"):
            issue = {
                "file": str(filename),
                "line": violation.attrib.get("beginline", "?"),
                "priority": int(violation.attrib.get("priority", 5)),
                "rule": violation.attrib.get("rule", "Unknown Rule"),
                "message": violation.text.strip() if violation.text else "",
            }
            issues.append(issue)
    return issues


def group_issues_by_priority_and_file(issues):
    grouped = defaultdict(lambda: defaultdict(list))
    for issue in issues:
        grouped[issue["priority"]][issue["file"]].append(issue)
    return grouped

def generate_html(grouped_issues):
    html = [
        "<html><head><meta charset='UTF-8'>",
        "<style>",
        "body { font-family: Arial; margin: 20px; }",
        "h2 { color: #2c3e50; }",
        "h3 { margin-top: 10px; color: #34495e; }",
        "table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }",
        "th, td { border: 1px solid #ddd; padding: 8px; }",
        "th { background-color: #f2f2f2; text-align: left; }",
        "tr:nth-child(even) { background-color: #f9f9f9; }",
        "</style></head><body>",
        "<h1>📋 PMD Report - Grouped by Priority and Class</h1>",
    ]

    for priority in sorted(grouped_issues.keys()):
        html.append(f"<h2>🚨 Priority {priority}</h2>")
        for file in sorted(grouped_issues[priority].keys()):
            html.append(f"<h3>📄 {file}</h3>")
            html.append("<table><tr><th>Line</th><th>Rule</th><th>Description</th></tr>")
            for issue in grouped_issues[priority][file]:
                html.append(f"<tr><td>{issue['line']}</td><td>{escape(issue['rule'])}</td><td>{escape(issue['message'])}</td></tr>")
            html.append("</table>")

    html.append("</body></html>")
    return "\n".join(html)

if __name__ == "__main__":
    print(f"📁 Looking for reports in: {REPORT_DIR}")
    issues = []

    # Parse PMD XML reports
    issues += parse_pmd_xml(REPORT_DIR / "main.xml")
    issues += parse_pmd_xml(REPORT_DIR / "test.xml")

    print(f"🛠️ Total issues parsed: {len(issues)}")

    grouped = group_issues_by_priority_and_file(issues)
    html = generate_html(grouped)

    # Ensure output directory exists
    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    with open(OUTPUT_FILE, "w") as f:
        f.write(html)

    print(f" Grouped PMD report written to: {OUTPUT_FILE}")
