from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer

estilos = getSampleStyleSheet()
doc = SimpleDocTemplate("docs/etapa1-definicao-projeto.pdf", pagesize=A4,
                        leftMargin=60, rightMargin=60, topMargin=60, bottomMargin=60)

itens = [
    Paragraph("Projeto de VVS - Definição do Projeto", estilos["Title"]),
    Spacer(1, 12),
    Paragraph("<b>Aluno:</b> ______________________________", estilos["Normal"]),
    Spacer(1, 10),
    Paragraph("<b>Sistema:</b> Biblioteca", estilos["Normal"]),
    Spacer(1, 6),
    Paragraph("<b>Descrição:</b> sistema para cadastrar livros e controlar empréstimos "
              "e devoluções.", estilos["Normal"]),
    Spacer(1, 6),
    Paragraph("<b>Linguagem:</b> Java", estilos["Normal"]),
    Spacer(1, 6),
    Paragraph("<b>Framework:</b> Quarkus", estilos["Normal"]),
]
doc.build(itens)
