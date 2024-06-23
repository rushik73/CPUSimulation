Name: Rushik Guduru

Date: 03/02/2024

Section: CS 4348.501

Project Summary: Simulated CPU

Project Purpose

The primary goal of my project was to simulate a simple computer system, consisting of a CPU and Memory, that communicates through a predefined instruction set. The purpose extended beyond just creating a simulation; it aimed to delve into how multiple processes can communicate and cooperate, offering a practical understanding of low-level concepts crucial to an operating system. These concepts included processor interaction with main memory, processor instruction behavior, the role of registers, and notably, the execution of a program that outputs ASCII art, serving as demonstration of these interactions.

The project was executed in two main phases: designing the Memory class to handle data storage and retrieval, and developing the CPU class to interpret and execute instructions. The Memory class was straightforward, tasked with reading from and writing to specific addresses. It began with loading a program from an input file into its array, setting the stage for execution. The CPU class was more intricate. It involved interpreting a set of instructions to perform various operations, such as arithmetic calculations, memory access, and outputting characters to form outputs for the specific sample text. A significant part of the CPU's functionality was dedicated to executing a program that generates ASCII art, starting with a simple "Hello, World!" and advancing to more complex images, like a house. The project's implementation involved the creative use of Java, particularly leveraging its capabilities to simulate the processes of a CPU and Memory. Despite Java's high-level nature, the challenge lay in mimicking the low-level operations that occur within a computer's CPU and memory. This included executing Java programs that communicate through standard input/output streams, avoiding the use of threads or sockets to adhere to the project's constraints. By using these streams, I was able to simulate the communication between the CPU and Memory processes in a manner that mimicked the intricacies of real-world computer systems. The reliance on standard input/output streams required a nuanced understanding of how data flows between processes in Java.

Personal Experience

In summary, this project was a profound learning experience that extended beyond the technical skills developed. It fostered a deeper appreciation for the complexity of computer systems and the ingenuity required to simulate them. The journey from conceptualization to implementation, though fraught with challenges, was immensely rewarding, offering invaluable insights into the inner workings of CPUs and memory and the art of programming at a low level. Throughout this project, I encountered moments of frustration, especially when dealing with the intricacies of simulating hardware operations in software. However, these challenges were instrumental in deepening my understanding of computer architecture. I learned the value of patience and meticulous planning, as each instruction had to be carefully crafted to achieve the desired outcome.




